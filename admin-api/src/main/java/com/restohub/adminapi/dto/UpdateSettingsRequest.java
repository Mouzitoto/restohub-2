package com.restohub.adminapi.dto;

import lombok.Data;

@Data
public class UpdateSettingsRequest {
    private String systemName;
    private String whatsappNumber;
    private String whatsappToken;
}

