package com.parasol.rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.parasol.ejb.ClaimService;
import com.parasol.model.Claim;

@Path("/api/claims")
@Produces(MediaType.APPLICATION_JSON)
public class ClaimsResource {

    @EJB
    private ClaimService claimService;

    @GET
    public List<Claim> getAllClaims() {
        return claimService.findAll();
    }

    @GET
    @Path("/{claimNumber}")
    public Response getClaim(@PathParam("claimNumber") String claimNumber) {
        Claim claim = claimService.findByClaimNumber(claimNumber);
        if (claim != null) {
            return Response.ok(claim).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
