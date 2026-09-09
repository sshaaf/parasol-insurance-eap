package com.parasol.mdb;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.parasol.ejb.EmailRoutingService;
import com.parasol.ejb.EmailStoreBean;
import com.parasol.model.Email;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/email-intake"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
})
public class EmailIntakeMDB implements MessageListener {

    private static final Logger LOG = Logger.getLogger(EmailIntakeMDB.class.getName());

    @EJB
    private EmailRoutingService routingService;

    @EJB
    private EmailStoreBean emailStore;

    @Override
    public void onMessage(Message message) {
        try {
            String json = ((TextMessage) message).getText();
            Email email = routingService.route(json);
            emailStore.add(email);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to process email", e);
        }
    }
}
