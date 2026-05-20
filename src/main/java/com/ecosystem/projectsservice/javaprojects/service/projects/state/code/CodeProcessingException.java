package com.ecosystem.projectsservice.javaprojects.service.projects.state.code;

import com.ecosystem.projectsservice.javaprojects.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class CodeProcessingException extends BaseException {
    public CodeProcessingException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
