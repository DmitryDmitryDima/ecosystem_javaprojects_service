package com.ecosystem.projectsservice.javaprojects.service.storage;

import com.ecosystem.projectsservice.javaprojects.common_exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class StorageException extends BaseException {


    public StorageException(String message, String errorCode,  HttpStatus status) {
        super(message, errorCode, status);
    }


    public StorageException(String message){
        super(message, "STORAGE_EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
