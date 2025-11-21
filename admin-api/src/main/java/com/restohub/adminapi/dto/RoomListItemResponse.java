package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class RoomListItemResponse {
    private Long id;
    private Long restaurantId;
    private Long floorId;
    private String name;
    private String description;
    private Boolean isSmoking;
    private Boolean isOutdoor;
    private Long imageId;
    private Integer tableCount;
    private Boolean isActive;
    private Instant createdAt;
}

