package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.exceptions;

import com.ecosystem.projectsservice.javaprojects.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class ChainStateException extends BaseException {

    public ChainStateException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
