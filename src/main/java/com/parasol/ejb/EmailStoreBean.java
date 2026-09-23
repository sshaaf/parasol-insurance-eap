package com.parasol.ejb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.parasol.model.Email;

@ApplicationScoped
@Transactional
public class EmailStoreBean {

    private final AtomicLong idSequence = new AtomicLong(0);
    private final CopyOnWriteArrayList<Email> emails = new CopyOnWriteArrayList<>();

    public void add(Email email) {
        if (email.getId() == 0) {
            email.setId(idSequence.incrementAndGet());
        }
        emails.add(email);
    }

    public List<Email> all() {
        List<Email> result = new ArrayList<>(emails);
        Collections.reverse(result);
        return result;
    }

    public List<Email> after(long id) {
        List<Email> result = new ArrayList<>();
        for (Email email : emails) {
            if (email.getId() > id) {
                result.add(email);
            }
        }
        Collections.reverse(result);
        return result;
    }
}