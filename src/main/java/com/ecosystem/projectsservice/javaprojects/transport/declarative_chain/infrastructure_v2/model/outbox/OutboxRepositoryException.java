package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.model.outbox;

public class OutboxRepositoryException extends RuntimeException {
    public OutboxRepositoryException(String message) {
        super(message);
    }
}
