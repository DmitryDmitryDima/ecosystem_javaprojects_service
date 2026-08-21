package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.model.outbox;

public class OutboxRepositoryException extends RuntimeException {
    public OutboxRepositoryException(String message) {
        super(message);
    }
}
