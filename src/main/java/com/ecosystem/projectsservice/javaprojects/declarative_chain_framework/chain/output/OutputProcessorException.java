package com.ecosystem.projectsservice.javaprojects.declarative_chain_framework.chain.output;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class OutputProcessorException extends RuntimeException {
    public OutputProcessorException(String message) {

        super(message);
    }
}
