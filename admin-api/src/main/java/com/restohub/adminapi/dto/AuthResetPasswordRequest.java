package com.restohub.adminapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthResetPasswordRequest {
    
    @NotBlank(message = "Email обязателен")
    @jakarta.validation.constraints.Email(message = "Email должен быть валидным")
    private String email;
    
    @NotBlank(message = "Код обязателен")
    @Size(min = 6, max = 6, message = "Код должен содержать 6 символов")
    private String code;
    
    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    private String newPassword;
}

