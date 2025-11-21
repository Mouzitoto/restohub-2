package com.restohub.adminapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    
    private static final String PHONE_PATTERN = "^(\\+7|8)[0-9]{10}$";
    
    @Override
    public void initialize(Phone constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // null проверяется через @NotNull
        }
        
        return phone.matches(PHONE_PATTERN);
    }
    
    /**
     * Нормализует номер телефона к формату +7XXXXXXXXXX
     */
    public static String normalize(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }
        
        // Удаляем все пробелы и дефисы
        String normalized = phone.replaceAll("[\\s-]", "");
        
        // Если начинается с 8, заменяем на +7
        if (normalized.startsWith("8") && normalized.length() == 11) {
            normalized = "+7" + normalized.substring(1);
        }
        
        return normalized;
    }
}

