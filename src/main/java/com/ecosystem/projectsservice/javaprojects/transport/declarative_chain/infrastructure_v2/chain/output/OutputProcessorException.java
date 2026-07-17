package com.ecosystem.projectsservice.javaprojects.transport.declarative_chain.infrastructure_v2.chain.output;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class OutputProcessorException extends RuntimeException {
    public OutputProcessorException(String message) {

        super(message);
    }
}
