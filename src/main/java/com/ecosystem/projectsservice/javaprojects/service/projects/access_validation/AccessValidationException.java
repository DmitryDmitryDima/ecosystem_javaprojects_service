package com.ecosystem.projectsservice.javaprojects.service.projects.access_validation;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class AccessValidationException extends BaseException {
    public AccessValidationException(String message) {
        super(message, "PROJECT_ACCESS_DENIED",
                HttpStatus.FORBIDDEN);
    }
}
