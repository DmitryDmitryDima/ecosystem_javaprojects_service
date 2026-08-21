package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.managers.event_manager;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class EventManagerException extends RuntimeException {


    public EventManagerException(String message) {
        super(message);
    }


}
