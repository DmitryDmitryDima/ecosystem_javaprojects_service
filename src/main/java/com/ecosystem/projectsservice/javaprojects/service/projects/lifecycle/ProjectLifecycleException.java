package com.ecosystem.projectsservice.javaprojects.service.projects.lifecycle;


import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class ProjectLifecycleException extends BaseException {


    public ProjectLifecycleException(String message,
                                     String errorCode,
                                     HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
