package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingListItemResponse {
    private Long id;
    private Long restaurantId;
    private Long tableId;
    private String tableNumber;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private Integer personCount;
    private String clientName;
    private String specialRequests;
    private BookingStatusInfo status;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    public static class BookingStatusInfo {
        private String code;
        private String name;
    }
}

