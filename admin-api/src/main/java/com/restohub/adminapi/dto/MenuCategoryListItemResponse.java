package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class MenuCategoryListItemResponse {
    private Long id;
    private String name;
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
    private Instant createdAt;
}

