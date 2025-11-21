package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class FloorResponse {
    private Long id;
    private Long restaurantId;
    private String floorNumber;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}

