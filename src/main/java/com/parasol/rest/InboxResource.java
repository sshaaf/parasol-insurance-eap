package com.parasol.rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.parasol.ejb.EmailStoreBean;
import com.parasol.model.Email;

@Path("/api/inbox")
@Produces(MediaType.APPLICATION_JSON)
public class InboxResource {

    @EJB
    private EmailStoreBean emailStore;

    @GET
    public List<Email> getEmails(@QueryParam("after") Long after) {
        if (after != null) {
            return emailStore.after(after);
        }
        return emailStore.all();
    }
}
