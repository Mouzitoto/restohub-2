package com.restohub.clientapi.dto;

import com.restohub.clientapi.validation.Phone;
import com.restohub.clientapi.validation.ValidBookingId;
import com.restohub.clientapi.validation.ValidRestaurantId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePreOrderRequest {
    
    @NotNull(message = "Restaurant ID is required")
    @ValidRestaurantId
    private Long restaurantId;
    
    @ValidBookingId(restaurantId = 0) // restaurantId будет установлен из restaurantId
    private Long bookingId;
    
    @NotNull(message = "Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in format YYYY-MM-DD")
    private String date;
    
    @NotNull(message = "Time is required")
    @Pattern(regexp = "\\d{2}:\\d{2}:\\d{2}", message = "Time must be in format HH:mm:ss")
    private String time;
    
    @NotNull(message = "Phone is required")
    @Phone
    private String clientPhone;
    
    @Size(max = 255, message = "Client first name must not exceed 255 characters")
    private String clientFirstName;
    
    @Size(max = 255, message = "Client name must not exceed 255 characters")
    private String clientName;
    
    @NotNull(message = "Items are required")
    @NotEmpty(message = "Items must not be empty")
    @Valid
    private List<PreOrderItemRequest> items;
    
    @Size(max = 10000, message = "Special requests must not exceed 10000 characters")
    private String specialRequests;
    
    @Size(max = 255, message = "WhatsApp message ID must not exceed 255 characters")
    private String whatsappMessageId;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreOrderItemRequest {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        @Size(max = 10000, message = "Special requests must not exceed 10000 characters")
        private String specialRequests;
    }
}

