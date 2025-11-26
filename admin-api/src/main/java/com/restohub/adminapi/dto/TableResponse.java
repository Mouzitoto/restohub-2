package com.restohub.adminapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TableResponse {
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
    private Instant updatedAt;
    private Instant deletedAt;
    private BigDecimal positionX1;
    private BigDecimal positionY1;
    private BigDecimal positionX2;
    private BigDecimal positionY2;
}

