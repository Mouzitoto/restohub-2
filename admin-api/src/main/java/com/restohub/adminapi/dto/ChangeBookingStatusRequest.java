package com.restohub.adminapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeBookingStatusRequest {
    
    @NotNull(message = "Status is required")
    private String status; // APPROVED, REJECTED
    
    @NotNull(message = "Manager ID is required")
    private Long managerId;
}

