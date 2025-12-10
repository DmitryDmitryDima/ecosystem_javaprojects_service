package com.ecosystem.projectsservice.javaprojects.advice;


import com.ecosystem.projectsservice.javaprojects.exceptions.AccessDeniedException;
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

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class})
    public ExceptionMessage handleAccessDeniedException(Exception e){
        ExceptionMessage exceptionMessage = new ExceptionMessage();
        exceptionMessage.setMessage(e.getMessage());
        return exceptionMessage;
    }
}
