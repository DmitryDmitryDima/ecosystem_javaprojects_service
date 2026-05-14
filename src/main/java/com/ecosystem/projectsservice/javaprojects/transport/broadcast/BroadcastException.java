package com.ecosystem.projectsservice.javaprojects.transport.broadcast;

import com.ecosystem.projectsservice.javaprojects.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class BroadcastException extends BaseException {


    public BroadcastException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
