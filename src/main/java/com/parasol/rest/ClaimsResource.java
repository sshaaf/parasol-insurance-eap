package com.parasol.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.exception.AuditLoggingException;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.FileSystemAuditLogger;
import com.parasol.ejb.ClaimService;
import com.parasol.model.Claim;

/**
 * Claims REST API with v1 enterprise audit-logging-library usage.
 * Patterns mirror the Konveyor inventory-management audit example so custom
 * Kantra rules (audit-logging-0002..0005) can detect migration issues.
 */
@Path("/api/claims")
@Produces(MediaType.APPLICATION_JSON)
public class ClaimsResource {

    private static final String APPLICATION = "ParasolInsurance";
    private static final String COMPONENT = "ClaimsResource";

    @EJB
    private ClaimService claimService;

    private FileSystemAuditLogger auditLogger;

    @PostConstruct
    public void init() {
        try {
            AuditConfiguration config = new AuditConfiguration();
            config.setLogDirectory("./parasol-claims-audit-logs");
            config.setAutoCreateDirectory(true);
            auditLogger = new FileSystemAuditLogger(config);
        } catch (AuditLoggingException e) {
            throw new IllegalStateException("Failed to initialize audit logger", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (auditLogger != null) {
            try {
                auditLogger.close();
            } catch (AuditLoggingException e) {
                // best-effort shutdown
            }
        }
    }

    @GET
    public List<Claim> getAllClaims() throws AuditLoggingException {
        List<Claim> claims = claimService.findAll();
        // Legacy convenience API (custom rule audit-logging-0005)
        auditLogger.logSuccess(
                "CLAIM_LIST",
                "LIST",
                "claims",
                "Listed " + claims.size() + " claims");
        return claims;
    }

    @GET
    @Path("/{claimNumber}")
    public Response getClaim(@PathParam("claimNumber") String claimNumber) throws AuditLoggingException {
        Claim claim = claimService.findByClaimNumber(claimNumber);
        if (claim == null) {
            // Legacy convenience API (custom rule audit-logging-0005)
            auditLogger.logFailure(
                    "CLAIM_VIEW",
                    "VIEW",
                    "claims/" + claimNumber,
                    "Claim not found: " + claimNumber);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<String, Object> details = new HashMap<>();
        details.put("claim_number", claimNumber);
        details.put("claim_id", claim.getId());

        // Builder pattern + synchronous logEvent (rules audit-logging-0002, 0004)
        // FileSystemAuditLogger import/use (rule audit-logging-0003)
        AuditEvent auditEvent = AuditEvent.builder()
                .eventType("CLAIM_VIEW")
                .userId("claims-api")
                .sessionId(UUID.randomUUID().toString())
                .application(APPLICATION)
                .component(COMPONENT)
                .action("VIEW")
                .resource("claims/" + claimNumber)
                .result(AuditResult.SUCCESS)
                .message("Viewed claim: " + claimNumber)
                .details(details)
                .correlationId(UUID.randomUUID().toString())
                .build();

        auditLogger.logEvent(auditEvent);
        return Response.ok(claim).build();
    }
}
