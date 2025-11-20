package com.restohub.adminapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token обязателен")
    private String refreshToken;
}

