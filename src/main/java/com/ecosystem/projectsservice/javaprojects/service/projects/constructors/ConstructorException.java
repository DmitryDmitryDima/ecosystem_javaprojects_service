package com.ecosystem.projectsservice.javaprojects.service.projects.constructors;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class ConstructorException extends BaseException {
    public ConstructorException(String message,
                                String errorCode,
                                HttpStatus httpStatus) {

        super(message, errorCode, httpStatus);
    }

    public ConstructorException(String message){

        super(message, "PROJECT_CONTRUCTOR_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
