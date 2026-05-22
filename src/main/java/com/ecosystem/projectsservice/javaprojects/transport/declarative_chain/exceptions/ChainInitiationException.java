package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.exceptions;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class ChainInitiationException extends BaseException {


    public ChainInitiationException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
