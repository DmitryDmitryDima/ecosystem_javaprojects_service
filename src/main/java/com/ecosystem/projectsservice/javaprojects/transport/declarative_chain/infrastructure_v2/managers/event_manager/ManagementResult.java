package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager;

public class ManagementResult {


    // общий маркер успеха - ивент расшифрован и отправлен через sender
    private boolean success;


    private RuntimeException exception;

    public boolean isSuccess() {
        return success;
    }

    public RuntimeException getException() {
        return exception;
    }
}
