package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class BookingStatusResponse {
    private Long id;
    private String code;
    private String name;
    private Integer displayOrder;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}

