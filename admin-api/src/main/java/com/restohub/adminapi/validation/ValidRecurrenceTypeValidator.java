package com.restohub.adminapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidRecurrenceTypeValidator implements ConstraintValidator<ValidRecurrenceType, String> {
    
    @Override
    public void initialize(ValidRecurrenceType constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String recurrenceType, ConstraintValidatorContext context) {
        if (recurrenceType == null || recurrenceType.trim().isEmpty()) {
            return true; // null проверяется через другие валидаторы
        }
        
        String upperCase = recurrenceType.trim().toUpperCase();
        return "WEEKLY".equals(upperCase) || 
               "MONTHLY".equals(upperCase) || 
               "DAILY".equals(upperCase);
    }
}

