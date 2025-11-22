package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.SettingsResponse;
import com.restohub.adminapi.dto.UpdateSettingsRequest;
import com.restohub.adminapi.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class SettingsController {
    
    private final SettingsService settingsService;
    
    @Autowired
    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettingsResponse> getSettings() {
        SettingsResponse response = settingsService.getSettings();
        return ResponseEntity.ok(response);
    }
    
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettingsResponse> updateSettings(@RequestBody UpdateSettingsRequest request) {
        SettingsResponse response = settingsService.updateSettings(request);
        return ResponseEntity.ok(response);
    }
}

