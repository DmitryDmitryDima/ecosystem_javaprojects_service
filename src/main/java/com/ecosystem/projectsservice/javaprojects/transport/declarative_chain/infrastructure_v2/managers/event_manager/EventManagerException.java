package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers.event_manager;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class EventManagerException extends RuntimeException {


    public EventManagerException(String message) {
        super(message);
    }


}
