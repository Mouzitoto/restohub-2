package com.restohub.adminapi.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTablePositionRequest {
    
    @NotNull(message = "ID стола обязателен")
    private Long tableId;
    
    @DecimalMin(value = "0.0", message = "Координата X1 должна быть от 0 до 100")
    @DecimalMax(value = "100.0", message = "Координата X1 должна быть от 0 до 100")
    private BigDecimal positionX1;
    
    @DecimalMin(value = "0.0", message = "Координата Y1 должна быть от 0 до 100")
    @DecimalMax(value = "100.0", message = "Координата Y1 должна быть от 0 до 100")
    private BigDecimal positionY1;
    
    @DecimalMin(value = "0.0", message = "Координата X2 должна быть от 0 до 100")
    @DecimalMax(value = "100.0", message = "Координата X2 должна быть от 0 до 100")
    private BigDecimal positionX2;
    
    @DecimalMin(value = "0.0", message = "Координата Y2 должна быть от 0 до 100")
    @DecimalMax(value = "100.0", message = "Координата Y2 должна быть от 0 до 100")
    private BigDecimal positionY2;
}

