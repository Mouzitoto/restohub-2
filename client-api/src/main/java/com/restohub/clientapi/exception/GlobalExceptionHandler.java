package com.restohub.clientapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        String exceptionName = e.getMessage();
        String traceId = MDC.get("traceId");
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        // Определяем статус на основе exceptionName
        if ("TOO_MANY_REQUESTS".equals(exceptionName)) {
            status = HttpStatus.TOO_MANY_REQUESTS;
        } else if ("CIRCUIT_BREAKER_OPEN".equals(exceptionName)) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        
        logger.error("Exception: {}", exceptionName, e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                exceptionName != null ? exceptionName : "INTERNAL_SERVER_ERROR",
                e.getMessage() != null ? e.getMessage() : "Внутренняя ошибка сервера",
                Instant.now().toString(),
                traceId
        );
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String traceId = MDC.get("traceId");
        String message = "Ошибка валидации: " + errors.toString();
        
        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                message,
                Instant.now().toString(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        String traceId = MDC.get("traceId");
        
        logger.error("Unexpected error", e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Внутренняя ошибка сервера",
                Instant.now().toString(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    public record ErrorResponse(
            String exceptionName,
            String message,
            String timestamp,
            String traceId
    ) {}
}

