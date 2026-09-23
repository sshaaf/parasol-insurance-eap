package com.parasol.mdb;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.parasol.ejb.EmailRoutingService;
import com.parasol.ejb.EmailStoreBean;
import com.parasol.model.Email;

@ApplicationScoped
public class EmailIntakeMDB {

    private static final Logger LOG = Logger.getLogger(EmailIntakeMDB.class.getName());

    @Inject
    private EmailRoutingService routingService;

    @Inject
    private EmailStoreBean emailStore;

    @Incoming("email-intake")
    @Transactional
    public void onMessage(String json) {
        try {
            Email email = routingService.route(json);
            emailStore.add(email);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to process email", e);
        }
    }
}
