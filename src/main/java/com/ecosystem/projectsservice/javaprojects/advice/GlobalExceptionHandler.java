package com.ecosystem.projectsservice.javaprojects.advice;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ExceptionMessage handleIllegalArgumentException(Exception e){

        ExceptionMessage em = new ExceptionMessage();

        em.setMessage(e.getMessage());
        return em;

    }
}
