package com.parasol.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
@Startup
public class EmailGeneratorBean {

    private static final int MAX_EMAILS = 15;
    private static final String EMAILS_RESOURCE = "emails/sample-emails.json";
    private static final Random RANDOM = new Random();
    private static final Logger LOG = Logger.getLogger(EmailGeneratorBean.class.getName());

    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicInteger sentCount = new AtomicInteger(0);
    private List<String> emails = Collections.emptyList();

    @Resource(lookup = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "java:/jms/queue/email-intake")
    private Queue emailQueue;

    @Resource
    private TimerService timerService;

    @PostConstruct
    void init() {
        emails = loadEmails();
        timerService.createIntervalTimer(5000, 45000, new TimerConfig("email-generator", false));
    }

    @Timeout
    public void generate(Timer timer) {
        if (emails.isEmpty() || sentCount.get() >= MAX_EMAILS) {
            return;
        }

        String email = emails.get(RANDOM.nextInt(emails.size()));
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             MessageProducer producer = session.createProducer(emailQueue)) {

            TextMessage message = session.createTextMessage(email);
            producer.send(message);
            sentCount.incrementAndGet();
        } catch (JMSException e) {
            LOG.log(Level.SEVERE, "Failed to publish sample email", e);
        }
    }

    private List<String> loadEmails() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(EMAILS_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(EMAILS_RESOURCE + " not found on classpath");
            }
            JsonNode array = mapper.readTree(in);
            List<String> result = new ArrayList<>(array.size());
            for (JsonNode node : array) {
                result.add(mapper.writeValueAsString(node));
            }
            LOG.info("Loaded " + result.size() + " sample emails from " + EMAILS_RESOURCE);
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + EMAILS_RESOURCE, e);
        }
    }
}
