package com.restohub.adminapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class RestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String whatsapp;
    private String instagram;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String workingHours;
    private String managerLanguageCode;
    private Long logoImageId;
    private Long bgImageId;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}

