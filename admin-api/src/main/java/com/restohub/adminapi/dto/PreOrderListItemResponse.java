package com.restohub.adminapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PreOrderListItemResponse {
    private Long id;
    private Long restaurantId;
    private Long bookingId;
    private LocalDate date;
    private LocalTime time;
    private String clientName;
    private BigDecimal totalAmount;
    private String specialRequests;
    private BookingStatusInfo status;
    private Integer itemsCount;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    public static class BookingStatusInfo {
        private String code;
        private String name;
    }
}

