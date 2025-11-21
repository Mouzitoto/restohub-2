package com.restohub.adminapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class TestExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        if (message != null) {
            if (message.contains("NOT_FOUND")) {
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("IN_USE") || message.contains("REQUIRED") || 
                       message.contains("TOO_LARGE") || message.contains("INVALID")) {
                status = HttpStatus.BAD_REQUEST;
            }
        }
        
        return ResponseEntity.status(status).body(message);
    }
}

