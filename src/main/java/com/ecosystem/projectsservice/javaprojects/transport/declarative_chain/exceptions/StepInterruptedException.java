package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.exceptions;

import com.ecosystem.projectsservice.javaprojects.exceptions.BaseException;

public class StepInterruptedException extends RuntimeException {

    public StepInterruptedException(String message){
        super(message);
    }
}
