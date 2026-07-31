package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.structure.exception;

public class StepStoppedDuringExecutionException extends RuntimeException {
    public StepStoppedDuringExecutionException(String message) {
        super(message);
    }
}
