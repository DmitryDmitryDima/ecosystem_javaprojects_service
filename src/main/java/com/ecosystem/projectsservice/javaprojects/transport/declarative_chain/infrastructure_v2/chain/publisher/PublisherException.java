package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.publisher;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class PublisherException extends BaseException {
    public PublisherException(String message,
                              String errorCode) {

        super(message,
                errorCode,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
