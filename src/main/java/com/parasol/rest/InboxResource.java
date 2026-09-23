package com.parasol.rest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.transaction.Transactional;

import com.parasol.ejb.EmailStoreBean;
import com.parasol.model.Email;

@Path("/api/inbox")
@Produces(MediaType.APPLICATION_JSON)
public class InboxResource {

    @Inject
    private EmailStoreBean emailStore;

    @GET
    @Transactional
    public List<Email> getEmails(@QueryParam("after") Long after) {
        if (after != null) {
            return emailStore.after(after);
        }
        return emailStore.all();
    }
}