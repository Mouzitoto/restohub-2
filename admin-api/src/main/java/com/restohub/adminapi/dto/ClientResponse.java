package com.restohub.adminapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ClientResponse {
    private Long id;
    private String phone;
    private String firstName;
    private Integer totalBookings;
    private Integer totalPreOrders;
    private Instant firstBookingDate;
    private Instant lastBookingDate;
    private ClientStatistics statistics;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    public static class ClientStatistics {
        private Integer averageBookingPersons;
        private Long favoriteTableId;
        private String favoriteTableNumber;
        private Long favoriteMenuItemId;
        private String favoriteMenuItemName;
        private BigDecimal averagePreOrderAmount;
    }
}

