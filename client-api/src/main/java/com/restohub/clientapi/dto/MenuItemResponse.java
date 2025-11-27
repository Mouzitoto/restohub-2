package com.restohub.clientapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private String ingredients;
    private BigDecimal price;
    private Integer discountPercent;
    private Integer spicinessLevel;
    private Boolean hasSugar;
    private Long imageId;
    private Integer displayOrder;
}

