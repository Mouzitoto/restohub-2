package com.restohub.adminapi.controller;

import com.restohub.adminapi.exception.GlobalExceptionHandler.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class TestExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        String exceptionName = e.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        // Определяем статус на основе exceptionName (как в GlobalExceptionHandler)
        if (exceptionName != null) {
            if ("INVALID_CREDENTIALS".equals(exceptionName) || 
                "INVALID_TOKEN".equals(exceptionName) ||
                "INVALID_REFRESH_TOKEN".equals(exceptionName) ||
                "TOKEN_EXPIRED".equals(exceptionName) ||
                "REFRESH_TOKEN_EXPIRED".equals(exceptionName) ||
                "UNAUTHORIZED".equals(exceptionName)) {
                status = HttpStatus.UNAUTHORIZED;
            } else if ("ACCESS_DENIED".equals(exceptionName)) {
                status = HttpStatus.FORBIDDEN;
            } else if (exceptionName.contains("NOT_FOUND")) {
                status = HttpStatus.NOT_FOUND;
            } else if (exceptionName.contains("IN_USE") || exceptionName.contains("REQUIRED") || 
                       exceptionName.contains("TOO_LARGE") || exceptionName.contains("INVALID") ||
                       exceptionName.contains("MISSING")) {
                status = HttpStatus.BAD_REQUEST;
            }
        }
        
        String message = getErrorMessage(exceptionName);
        ErrorResponse errorResponse = new ErrorResponse(
                exceptionName != null ? exceptionName : "INTERNAL_SERVER_ERROR",
                message,
                Instant.now().toString(),
                null
        );
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    private String getErrorMessage(String exceptionName) {
        if (exceptionName == null) return "Произошла ошибка";
        
        return switch (exceptionName) {
            case "INVALID_CREDENTIALS" -> "Неверный email или пароль";
            case "INVALID_TOKEN" -> "Невалидный токен";
            case "TOKEN_EXPIRED" -> "Токен истек";
            case "INVALID_REFRESH_TOKEN" -> "Невалидный refresh token";
            case "REFRESH_TOKEN_EXPIRED" -> "Refresh token истек";
            case "UNAUTHORIZED" -> "Требуется аутентификация";
            default -> "Произошла ошибка";
        };
    }
}

