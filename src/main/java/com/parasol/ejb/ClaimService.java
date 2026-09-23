package com.parasol.ejb;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import com.parasol.model.Claim;

@ApplicationScoped
@Transactional
public class ClaimService {

    @Inject
    private EntityManager entityManager;

    public List<Claim> findAll() {
        return entityManager.createQuery("SELECT c FROM Claim c", Claim.class).getResultList();
    }

    public Claim findByClaimNumber(String claimNumber) {
        List<Claim> results = entityManager
                .createQuery("SELECT c FROM Claim c WHERE c.claimNumber = :claimNumber", Claim.class)
                .setParameter("claimNumber", claimNumber)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}