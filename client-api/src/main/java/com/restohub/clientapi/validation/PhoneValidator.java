package com.restohub.clientapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\d{10}$|^8\\d{10}$");
    
    @Override
    public void initialize(Phone constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String normalized = phone.trim().replaceAll("[^0-9+]", "");
        if (normalized.startsWith("8")) {
            normalized = "+7" + normalized.substring(1);
        } else if (!normalized.startsWith("+7")) {
            normalized = "+7" + normalized;
        }
        return normalized;
    }
}

