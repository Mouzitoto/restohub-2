package com.restohub.adminapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
        if ("INVALID_CREDENTIALS".equals(exceptionName) || 
            "INVALID_TOKEN".equals(exceptionName) ||
            "INVALID_REFRESH_TOKEN".equals(exceptionName) ||
            "TOKEN_EXPIRED".equals(exceptionName) ||
            "REFRESH_TOKEN_EXPIRED".equals(exceptionName) ||
            "UNAUTHORIZED".equals(exceptionName)) {
            status = HttpStatus.UNAUTHORIZED;
        } else if ("ACCESS_DENIED".equals(exceptionName)) {
            status = HttpStatus.FORBIDDEN;
        } else if ("MISSING_CREDENTIALS".equals(exceptionName) ||
                   "MISSING_EMAIL".equals(exceptionName) ||
                   "MISSING_REFRESH_TOKEN".equals(exceptionName) ||
                   "MISSING_FIELDS".equals(exceptionName) ||
                   "INVALID_EMAIL".equals(exceptionName) ||
                   "INVALID_PASSWORD".equals(exceptionName) ||
                   "INVALID_RESET_CODE".equals(exceptionName) ||
                   "RESET_CODE_EXPIRED".equals(exceptionName) ||
                   "RESET_CODE_ALREADY_USED".equals(exceptionName)) {
            status = HttpStatus.BAD_REQUEST;
        } else if ("USER_NOT_FOUND".equals(exceptionName)) {
            status = HttpStatus.NOT_FOUND;
        }
        
        logger.error("Exception: {}", exceptionName, e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                exceptionName,
                getErrorMessage(exceptionName),
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
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        String traceId = MDC.get("traceId");
        
        ErrorResponse errorResponse = new ErrorResponse(
                "ACCESS_DENIED",
                "Доступ запрещен",
                Instant.now().toString(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
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
    
    private String getErrorMessage(String exceptionName) {
        return switch (exceptionName) {
            case "INVALID_CREDENTIALS" -> "Неверный email или пароль";
            case "MISSING_CREDENTIALS" -> "Email и пароль обязательны";
            case "INVALID_TOKEN" -> "Невалидный токен";
            case "TOKEN_EXPIRED" -> "Токен истек";
            case "INVALID_REFRESH_TOKEN" -> "Невалидный refresh token";
            case "REFRESH_TOKEN_EXPIRED" -> "Refresh token истек";
            case "MISSING_REFRESH_TOKEN" -> "Refresh token обязателен";
            case "UNAUTHORIZED" -> "Требуется аутентификация";
            case "ACCESS_DENIED" -> "Доступ запрещен";
            case "MISSING_EMAIL" -> "Email обязателен";
            case "INVALID_EMAIL" -> "Email невалиден";
            case "MISSING_FIELDS" -> "Не все обязательные поля заполнены";
            case "INVALID_PASSWORD" -> "Пароль должен содержать минимум 8 символов";
            case "INVALID_RESET_CODE" -> "Неверный код восстановления";
            case "RESET_CODE_EXPIRED" -> "Код восстановления истек";
            case "RESET_CODE_ALREADY_USED" -> "Код восстановления уже использован";
            case "USER_NOT_FOUND" -> "Пользователь не найден";
            default -> "Произошла ошибка";
        };
    }
    
    public record ErrorResponse(
            String exceptionName,
            String message,
            String timestamp,
            String traceId
    ) {}
}

