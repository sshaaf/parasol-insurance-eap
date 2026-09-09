package com.parasol.model;

import java.io.Serializable;

public class Email implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String from;
    private String subject;
    private String body;
    private String receivedAt;
    private String department;
    private String routingReason;

    public Email() {
    }

    public Email(long id, String from, String subject, String body, String receivedAt,
                 String department, String routingReason) {
        this.id = id;
        this.from = from;
        this.subject = subject;
        this.body = body;
        this.receivedAt = receivedAt;
        this.department = department;
        this.routingReason = routingReason;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRoutingReason() {
        return routingReason;
    }

    public void setRoutingReason(String routingReason) {
        this.routingReason = routingReason;
    }
}
