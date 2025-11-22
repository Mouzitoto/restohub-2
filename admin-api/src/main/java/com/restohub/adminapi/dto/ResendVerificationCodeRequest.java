package com.restohub.adminapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendVerificationCodeRequest {
    
    @NotBlank(message = "Email обязателен")
    @Email(message = "Email должен быть валидным")
    private String email;
}

