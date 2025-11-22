package com.restohub.adminapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequest {
    
    @NotBlank(message = "Email обязателен")
    @Email(message = "Email должен быть валидным")
    private String email;
    
    @NotBlank(message = "Код обязателен")
    @Size(min = 4, max = 4, message = "Код должен содержать 4 символа")
    private String code;
}

