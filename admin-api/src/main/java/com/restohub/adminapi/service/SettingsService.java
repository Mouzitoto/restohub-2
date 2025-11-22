package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.SettingsResponse;
import com.restohub.adminapi.dto.UpdateSettingsRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Сервис для управления системными настройками.
 * Временная реализация в памяти. В будущем можно перенести в БД.
 */
@Service
public class SettingsService {
    
    private final ConcurrentMap<String, String> settings = new ConcurrentHashMap<>();
    
    public SettingsResponse getSettings() {
        SettingsResponse response = new SettingsResponse();
        response.setSystemName(settings.getOrDefault("systemName", "RestoHub"));
        response.setWhatsappNumber(settings.getOrDefault("whatsappNumber", ""));
        response.setWhatsappToken(settings.getOrDefault("whatsappToken", ""));
        return response;
    }
    
    public SettingsResponse updateSettings(UpdateSettingsRequest request) {
        if (request.getSystemName() != null) {
            settings.put("systemName", request.getSystemName());
        }
        if (request.getWhatsappNumber() != null) {
            settings.put("whatsappNumber", request.getWhatsappNumber());
        }
        if (request.getWhatsappToken() != null) {
            settings.put("whatsappToken", request.getWhatsappToken());
        }
        return getSettings();
    }
}

