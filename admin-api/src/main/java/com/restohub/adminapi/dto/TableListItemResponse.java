package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class TableListItemResponse {
    private Long id;
    private Long restaurantId;
    private Long roomId;
    private String tableNumber;
    private Integer capacity;
    private String description;
    private Long imageId;
    private String depositAmount;
    private String depositNote;
    private Boolean isActive;
    private Instant createdAt;
}

