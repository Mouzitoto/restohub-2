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
                   "RESET_CODE_ALREADY_USED".equals(exceptionName) ||
                   "FILE_REQUIRED".equals(exceptionName) ||
                   "FILE_TOO_LARGE".equals(exceptionName) ||
                   "INVALID_FILE_TYPE".equals(exceptionName) ||
                   "INVALID_IMAGE".equals(exceptionName) ||
                   "INVALID_IMAGE_TYPE".equals(exceptionName) ||
                   "IMAGE_UPLOAD_ERROR".equals(exceptionName) ||
                   "RESTAURANT_IN_USE".equals(exceptionName) ||
                   "IMAGE_IN_USE".equals(exceptionName) ||
                   "CATEGORY_NAME_EXISTS".equals(exceptionName) ||
                   "CATEGORY_IN_USE".equals(exceptionName) ||
                   "FLOOR_NUMBER_EXISTS".equals(exceptionName) ||
                   "FLOOR_IN_USE".equals(exceptionName) ||
                   "ROOM_IN_USE".equals(exceptionName) ||
                   "TABLE_NUMBER_EXISTS".equals(exceptionName) ||
                   "TABLE_HAS_ACTIVE_BOOKINGS".equals(exceptionName) ||
                   "BOOKING_STATUS_CODE_EXISTS".equals(exceptionName) ||
                   "BOOKING_STATUS_IN_USE".equals(exceptionName) ||
                   "INVALID_DATE_RANGE".equals(exceptionName) ||
                   "RECURRENCE_TYPE_REQUIRED".equals(exceptionName) ||
                   "INVALID_RECURRENCE_TYPE".equals(exceptionName) ||
                   "RECURRENCE_DAY_OF_WEEK_REQUIRED".equals(exceptionName) ||
                   "INVALID_RECURRENCE_DAY_OF_WEEK".equals(exceptionName) ||
                   "RECURRENCE_DAYS_OF_WEEK_REQUIRED".equals(exceptionName) ||
                   "INVALID_RECURRENCE_DAYS_OF_WEEK".equals(exceptionName) ||
                   "RECURRENCE_FIELDS_NOT_ALLOWED".equals(exceptionName) ||
                   "UNSUPPORTED_EXPORT_FORMAT".equals(exceptionName) ||
                   "INVALID_EXPORT_TYPE".equals(exceptionName) ||
                   "EMAIL_ALREADY_EXISTS".equals(exceptionName) ||
                   "MANAGER_MUST_HAVE_RESTAURANTS".equals(exceptionName) ||
                   "CANNOT_MODIFY_SELF".equals(exceptionName) ||
                   "CANNOT_DELETE_SELF".equals(exceptionName) ||
                   "CANNOT_DEACTIVATE_SELF".equals(exceptionName) ||
                   "BOOKING_ALREADY_CANCELLED_OR_REJECTED".equals(exceptionName) ||
                   "PRE_ORDER_ALREADY_CANCELLED_OR_REJECTED".equals(exceptionName) ||
                   "TABLE_POSITIONS_INTERSECT".equals(exceptionName) ||
                   "TABLE_NOT_IN_ROOM".equals(exceptionName) ||
                   "NO_ACTIVE_SUBSCRIPTION".equals(exceptionName)) {
            status = HttpStatus.BAD_REQUEST;
        } else if ("USER_NOT_FOUND".equals(exceptionName) ||
                   "RESTAURANT_NOT_FOUND".equals(exceptionName) ||
                   "IMAGE_NOT_FOUND".equals(exceptionName) ||
                   "CATEGORY_NOT_FOUND".equals(exceptionName) ||
                   "MENU_ITEM_NOT_FOUND".equals(exceptionName) ||
                   "FLOOR_NOT_FOUND".equals(exceptionName) ||
                   "ROOM_NOT_FOUND".equals(exceptionName) ||
                   "TABLE_NOT_FOUND".equals(exceptionName) ||
                   "BOOKING_STATUS_NOT_FOUND".equals(exceptionName) ||
                   "BOOKING_NOT_FOUND".equals(exceptionName) ||
                   "SUBSCRIPTION_TYPE_NOT_FOUND".equals(exceptionName) ||
                   "SUBSCRIPTION_NOT_FOUND".equals(exceptionName) ||
                   "PROMOTION_NOT_FOUND".equals(exceptionName) ||
                   "PROMOTION_TYPE_NOT_FOUND".equals(exceptionName) ||
                   "CLIENT_NOT_FOUND".equals(exceptionName) ||
                   "ROLE_NOT_FOUND".equals(exceptionName) ||
                   "PRE_ORDER_NOT_FOUND".equals(exceptionName)) {
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
            case "RESTAURANT_NOT_FOUND" -> "Ресторан не найден";
            case "RESTAURANT_IN_USE" -> "Ресторан используется и не может быть удален";
            case "IMAGE_NOT_FOUND" -> "Изображение не найдено";
            case "IMAGE_IN_USE" -> "Изображение используется и не может быть удалено";
            case "FILE_REQUIRED" -> "Файл обязателен";
            case "FILE_TOO_LARGE" -> "Файл слишком большой";
            case "INVALID_FILE_TYPE" -> "Неверный тип файла";
            case "INVALID_IMAGE" -> "Неверный формат изображения";
            case "INVALID_IMAGE_TYPE" -> "Неверный тип изображения. Допустимые значения: logo, background";
            case "IMAGE_UPLOAD_ERROR" -> "Ошибка при загрузке изображения";
            case "CATEGORY_NOT_FOUND" -> "Категория не найдена";
            case "CATEGORY_NAME_EXISTS" -> "Категория с таким названием уже существует";
            case "CATEGORY_IN_USE" -> "Категория используется и не может быть удалена";
            case "MENU_ITEM_NOT_FOUND" -> "Блюдо не найдено";
            case "FLOOR_NOT_FOUND" -> "Этаж не найден";
            case "FLOOR_NUMBER_EXISTS" -> "Этаж с таким номером уже существует для ресторана";
            case "FLOOR_IN_USE" -> "Этаж используется и не может быть удален";
            case "ROOM_NOT_FOUND" -> "Помещение не найдено";
            case "ROOM_IN_USE" -> "Помещение используется и не может быть удалено";
            case "TABLE_NOT_FOUND" -> "Стол не найден";
            case "TABLE_NUMBER_EXISTS" -> "Стол с таким номером уже существует для помещения";
            case "TABLE_HAS_ACTIVE_BOOKINGS" -> "Нельзя удалить стол: у стола есть активные бронирования";
            case "BOOKING_STATUS_NOT_FOUND" -> "Статус бронирования не найден";
            case "BOOKING_STATUS_CODE_EXISTS" -> "Статус с таким кодом уже существует";
            case "BOOKING_STATUS_IN_USE" -> "Статус используется и не может быть удален";
            case "SUBSCRIPTION_TYPE_NOT_FOUND" -> "Тип подписки не найден";
            case "SUBSCRIPTION_NOT_FOUND" -> "Подписка не найдена";
            case "INVALID_DATE_RANGE" -> "Неверный диапазон дат: дата окончания должна быть после даты начала";
            case "PROMOTION_NOT_FOUND" -> "Промо-событие не найдено";
            case "PROMOTION_TYPE_NOT_FOUND" -> "Тип промо-события не найден";
            case "RECURRENCE_TYPE_REQUIRED" -> "Тип повторения обязателен для повторяющихся событий";
            case "INVALID_RECURRENCE_TYPE" -> "Неверный тип повторения. Допустимые значения: WEEKLY, MONTHLY, DAILY";
            case "RECURRENCE_DAY_OF_WEEK_REQUIRED" -> "День недели обязателен для еженедельных повторений";
            case "INVALID_RECURRENCE_DAY_OF_WEEK" -> "Неверный день недели. Должен быть от 1 до 7";
            case "RECURRENCE_DAYS_OF_WEEK_REQUIRED" -> "Дни недели обязательны для еженедельных повторений. Выберите хотя бы один день";
            case "INVALID_RECURRENCE_DAYS_OF_WEEK" -> "Неверные дни недели. Все дни должны быть от 1 до 7 и не должны повторяться";
            case "RECURRENCE_FIELDS_NOT_ALLOWED" -> "Поля повторения не допускаются для неповторяющихся событий";
            case "CLIENT_NOT_FOUND" -> "Клиент не найден";
            case "BOOKING_NOT_FOUND" -> "Бронирование не найдено";
            case "PRE_ORDER_NOT_FOUND" -> "Предзаказ не найден";
            case "BOOKING_ALREADY_CANCELLED_OR_REJECTED" -> "Бронирование уже отменено или отклонено";
            case "PRE_ORDER_ALREADY_CANCELLED_OR_REJECTED" -> "Предзаказ уже отменен или отклонен";
            case "UNSUPPORTED_EXPORT_FORMAT" -> "Неподдерживаемый формат экспорта";
            case "INVALID_EXPORT_TYPE" -> "Неверный тип данных для экспорта";
            case "EXPORT_ERROR" -> "Ошибка при экспорте данных";
            case "EMAIL_ALREADY_EXISTS" -> "Пользователь с таким email уже существует";
            case "ROLE_NOT_FOUND" -> "Роль не найдена";
            case "MANAGER_MUST_HAVE_RESTAURANTS" -> "Менеджер должен быть привязан хотя бы к одному ресторану";
            case "CANNOT_MODIFY_SELF" -> "Нельзя изменять собственный профиль";
            case "CANNOT_DELETE_SELF" -> "Нельзя удалить самого себя";
            case "CANNOT_DEACTIVATE_SELF" -> "Нельзя деактивировать самого себя";
            case "TABLE_POSITIONS_INTERSECT" -> "Позиции столов пересекаются";
            case "TABLE_NOT_IN_ROOM" -> "Стол не принадлежит указанному залу";
            case "NO_ACTIVE_SUBSCRIPTION" -> "Нельзя активировать ресторан без активной подписки";
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

