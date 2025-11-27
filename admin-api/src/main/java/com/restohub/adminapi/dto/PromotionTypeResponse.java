package com.restohub.adminapi.dto;

import lombok.Data;

@Data
public class PromotionTypeResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
}

