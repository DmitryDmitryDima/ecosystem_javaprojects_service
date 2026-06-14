package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.managers;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class EventManagerException extends BaseException {


    public EventManagerException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }

    public EventManagerException(String message){
        this(message, "EVENT_MANAGER_EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
