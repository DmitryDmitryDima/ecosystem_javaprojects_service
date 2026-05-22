package com.ecosystem.projectsservice.javaprojects.service.projects.state.read;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class StateReadException extends BaseException {
    public StateReadException(String message, String errorCode, HttpStatus status) {
        super(message, errorCode, status);
    }
}
