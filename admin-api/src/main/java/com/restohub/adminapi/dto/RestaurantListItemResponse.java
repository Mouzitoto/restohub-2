package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class RestaurantListItemResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private Long logoImageId;
    private Boolean isActive;
    private Instant createdAt;
}

