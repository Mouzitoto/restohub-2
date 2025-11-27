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
public class TableResponse {
    private Long id;
    private String tableNumber;
    private Long roomId;
    private Integer capacity;
    private String description;
    private Long imageId;
    private String depositAmount;
    private String depositNote;
    private BigDecimal positionX1;
    private BigDecimal positionY1;
    private BigDecimal positionX2;
    private BigDecimal positionY2;
}

