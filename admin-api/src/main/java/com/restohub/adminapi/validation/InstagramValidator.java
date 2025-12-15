package com.restohub.adminapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InstagramValidator implements ConstraintValidator<Instagram, String> {
    
    @Override
    public void initialize(Instagram constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String instagram, ConstraintValidatorContext context) {
        if (instagram == null || instagram.trim().isEmpty()) {
            return true; // null проверяется через @NotNull или nullable
        }
        
        // Принимаем только username: буквы, цифры, точки, подчеркивания
        // Убираем @ если есть
        String trimmed = instagram.trim();
        if (trimmed.startsWith("@")) {
            trimmed = trimmed.substring(1);
        }
        
        // Username должен содержать только буквы, цифры, точки и подчеркивания
        // Не принимаем URL
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return false;
        }
        
        return trimmed.matches("^[a-zA-Z0-9._]+$");
    }
    
    /**
     * Нормализует Instagram к формату https://instagram.com/username
     */
    public static String normalize(String instagram) {
        if (instagram == null || instagram.trim().isEmpty()) {
            return instagram;
        }
        
        String trimmed = instagram.trim();
        
        // Если уже URL, возвращаем как есть (но нормализуем к https://instagram.com)
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            // Извлекаем username из URL
            String username = trimmed;
            if (trimmed.contains("instagram.com/")) {
                username = trimmed.substring(trimmed.indexOf("instagram.com/") + "instagram.com/".length());
                // Удаляем возможные параметры
                if (username.contains("?")) {
                    username = username.substring(0, username.indexOf("?"));
                }
                if (username.contains("/")) {
                    username = username.substring(0, username.indexOf("/"));
                }
            }
            return "https://instagram.com/" + username;
        }
        
        // Если это username (может быть с @ или без)
        String username = trimmed;
        if (username.startsWith("@")) {
            username = username.substring(1);
        }
        
        return "https://instagram.com/" + username;
    }
}

