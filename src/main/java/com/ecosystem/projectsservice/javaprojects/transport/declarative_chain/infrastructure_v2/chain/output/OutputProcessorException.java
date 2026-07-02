package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class OutputProcessorException extends BaseException {
    public OutputProcessorException(String message,
                                    String errorCode) {

        super(message,
                errorCode,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
