package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.exceptions;

public class StepInterruptedException extends RuntimeException{

    public StepInterruptedException(String message){
        super(message);
    }
}
