package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ClientListItemResponse {
    private Long id;
    private String phone;
    private String firstName;
    private Integer totalBookings;
    private Integer totalPreOrders;
    private Instant firstBookingDate;
    private Instant lastBookingDate;
    private Instant createdAt;
}

