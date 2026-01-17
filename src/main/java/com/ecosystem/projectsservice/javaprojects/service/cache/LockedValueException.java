package com.ecosystem.projectsservice.javaprojects.service.cache;

public class LockedValueException extends RuntimeException{
    public LockedValueException(String message){
        super(message);
    }
}
