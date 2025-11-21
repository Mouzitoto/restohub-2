package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class RoomResponse {
    private Long id;
    private Long restaurantId;
    private Long floorId;
    private String name;
    private String description;
    private Boolean isSmoking;
    private Boolean isOutdoor;
    private Long imageId;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}

