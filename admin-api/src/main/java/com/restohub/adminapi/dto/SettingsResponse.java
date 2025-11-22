package com.restohub.adminapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsResponse {
    private String systemName;
    private String whatsappNumber;
    private String whatsappToken;
}

