package com.restohub.adminapi.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {
    
    @NotBlank(message = "Email обязателен")
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    @Email(message = "Email должен быть валидным")
    private String email;
    
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    private String password;
    
    @NotNull(message = "ID роли обязателен")
    private Long roleId;
    
    private List<Long> restaurantIds;
}

