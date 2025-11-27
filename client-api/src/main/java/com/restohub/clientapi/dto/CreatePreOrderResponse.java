package com.restohub.clientapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePreOrderResponse {
    private Long id;
    private Long restaurantId;
    private Long bookingId;
    private LocalDate date;
    private LocalTime time;
    private Long clientId;
    private String clientName;
    private BigDecimal totalAmount;
    private Integer itemsCount;
    private BookingStatusResponse status;
    private LocalDateTime createdAt;
}

