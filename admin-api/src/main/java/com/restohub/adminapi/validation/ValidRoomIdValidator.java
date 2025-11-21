package com.restohub.adminapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidRoomIdValidator implements ConstraintValidator<ValidRoomId, Long> {
    
    @Override
    public void initialize(ValidRoomId constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(Long roomId, ConstraintValidatorContext context) {
        if (roomId == null) {
            return true; // null проверяется через @NotNull
        }
        
        // Проверка будет в сервисе, так как нужен restaurantId
        return true;
    }
}

