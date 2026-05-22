package com.ecosystem.projectsservice.javaprojects.common_exceptions;

import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends BaseException{

    public ProjectNotFoundException(String message) {
        super(message, "PROJECT_NOT_FOUND",
                HttpStatus.NOT_FOUND);
    }
}
