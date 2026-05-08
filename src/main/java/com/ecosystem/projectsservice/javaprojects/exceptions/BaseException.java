package com.ecosystem.projectsservice.javaprojects.exceptions;

import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException{

    private final HttpStatus httpStatus;
    private final String errorCode; // for example STORAGE_IO_EXCEPTION



    public BaseException(String message,
                         String errorCode,
                         HttpStatus httpStatus){
        super(message);

        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }


    public HttpStatus getHTTPStatus(){
        return httpStatus;
    }

    public String errorCode(){
        return errorCode;
    }




}
