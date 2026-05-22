package com.ecosystem.projectsservice.javaprojects.service.projects.state.update;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class StateUpdateException extends BaseException {
    public StateUpdateException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
