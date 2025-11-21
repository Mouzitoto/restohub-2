package com.restohub.adminapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MenuItemListItemResponse {
    private Long id;
    private Long restaurantId;
    private Long menuCategoryId;
    private String name;
    private String description;
    private String ingredients;
    private BigDecimal price;
    private Integer discountPercent;
    private BigDecimal finalPrice;
    private Integer spicinessLevel;
    private Boolean hasSugar;
    private Long imageId;
    private Integer displayOrder;
    private Boolean isActive;
    private Instant createdAt;
}

