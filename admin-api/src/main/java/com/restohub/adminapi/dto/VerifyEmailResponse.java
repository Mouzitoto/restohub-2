package com.restohub.adminapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailResponse {
    private String message;
    private Long userId;
    
    public VerifyEmailResponse(String message, Long userId) {
        this.message = message;
        this.userId = userId;
    }
}

