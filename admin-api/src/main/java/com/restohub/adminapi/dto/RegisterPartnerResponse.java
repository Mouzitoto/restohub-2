package com.restohub.adminapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterPartnerResponse {
    private String message;
    private String email;
    
    public RegisterPartnerResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }
}

