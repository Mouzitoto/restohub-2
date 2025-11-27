package com.restohub.clientapi.dto;

import com.restohub.clientapi.validation.ValidTableId;
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
public class CreateBookingRequest {
    
    @NotNull(message = "Table ID is required")
    @ValidTableId(restaurantId = 0) // restaurantId будет установлен в контроллере
    private Long tableId;
    
    @NotNull(message = "Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in format YYYY-MM-DD")
    private String date;
    
    @NotNull(message = "Time is required")
    @Pattern(regexp = "\\d{2}:\\d{2}:\\d{2}", message = "Time must be in format HH:mm:ss")
    private String time;
    
    @NotNull(message = "Person count is required")
    @Min(value = 1, message = "Person count must be at least 1")
    private Integer personCount;
    
    @Size(max = 255, message = "Client first name must not exceed 255 characters")
    private String clientFirstName;
    
    @Size(max = 255, message = "Client name must not exceed 255 characters")
    private String clientName;
    
    @Size(max = 10000, message = "Special requests must not exceed 10000 characters")
    private String specialRequests;
    
    @Valid
    private List<PreOrderItemRequest> preOrderItems;
    
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

