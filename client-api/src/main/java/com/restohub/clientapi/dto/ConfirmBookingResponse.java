package com.restohub.clientapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmBookingResponse {
    private Long id;
    private Long restaurantId;
    private Long tableId;
    private LocalDate date;
    private LocalTime time;
    private Integer personCount;
    private Long clientId;
    private String clientName;
    private String specialRequests;
    private BookingStatusResponse status;
    private Boolean whatsappNotificationSent;
    private LocalDateTime createdAt;
}

