package com.parasol.rest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;

import com.parasol.ejb.ClaimService;
import com.parasol.model.Claim;

@Path("/api/claims")
@Produces(MediaType.APPLICATION_JSON)
@Transactional
public class ClaimsResource {

    @Inject
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