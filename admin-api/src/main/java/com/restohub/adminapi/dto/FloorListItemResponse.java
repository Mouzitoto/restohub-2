package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class FloorListItemResponse {
    private Long id;
    private Long restaurantId;
    private String floorNumber;
    private Integer roomsCount;
    private Boolean isActive;
    private Instant createdAt;
}

