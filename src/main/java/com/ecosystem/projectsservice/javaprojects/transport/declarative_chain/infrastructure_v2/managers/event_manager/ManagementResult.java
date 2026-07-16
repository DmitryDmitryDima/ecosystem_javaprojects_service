package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager;

public class ManagementResult {


    // общий маркер успеха - ивент расшифрован и отправлен через sender
    private boolean success;

    private boolean compensationStart;


    private Exception exception;

    public boolean isSuccess() {
        return success;
    }

    public Exception getException() {
        return exception;
    }


    public ManagementResult(boolean success, Exception exception) {
        this.success = success;
        this.exception = exception;
    }

    public ManagementResult(boolean success, boolean compensationStart){
        this.success = success;
        this.compensationStart = compensationStart;
    }

    public boolean isCompensationStart() {
        return compensationStart;
    }
}
