package com.restohub.adminapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmBookingRequest {
    
    @NotNull(message = "Phone is required")
    private String phone;
    
    @Size(max = 255, message = "Client first name must not exceed 255 characters")
    private String clientFirstName;
    
    @Size(max = 255, message = "WhatsApp message ID must not exceed 255 characters")
    private String whatsappMessageId;
}

