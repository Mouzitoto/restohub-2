package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class RoleResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}

