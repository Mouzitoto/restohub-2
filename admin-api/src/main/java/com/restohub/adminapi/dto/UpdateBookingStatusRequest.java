package com.restohub.adminapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBookingStatusRequest {
    
    @NotNull(message = "ID статуса обязателен")
    private Long statusId;
    
    private String comment;
}
