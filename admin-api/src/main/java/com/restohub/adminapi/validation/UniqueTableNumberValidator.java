package com.restohub.adminapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueTableNumberValidator implements ConstraintValidator<UniqueTableNumber, String> {
    
    @Override
    public void initialize(UniqueTableNumber constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String tableNumber, ConstraintValidatorContext context) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            return true; // null проверяется через @NotNull
        }
        
        // Проверка будет в сервисе, так как нужен roomId
        return true;
    }
}

