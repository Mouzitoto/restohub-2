package com.restohub.adminapi.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
    
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    @Email(message = "Email должен быть валидным")
    private String email;
    
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    private String password;
    
    private Long roleId;
    
    private List<Long> restaurantIds;
}

