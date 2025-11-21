package com.restohub.adminapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class PreOrderResponse {
    private Long id;
    private Long restaurantId;
    private Long bookingId;
    private BookingInfo booking;
    private LocalDate date;
    private LocalTime time;
    private Long clientId;
    private String clientName;
    private BigDecimal totalAmount;
    private String specialRequests;
    private BookingStatusInfo status;
    private List<BookingHistoryItem> history;
    private List<PreOrderItem> items;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    public static class BookingInfo {
        private Long id;
        private LocalDate bookingDate;
        private LocalTime bookingTime;
        private String tableNumber;
    }
    
    @Data
    public static class BookingStatusInfo {
        private Long id;
        private String code;
        private String name;
    }
    
    @Data
    public static class BookingHistoryItem {
        private Long id;
        private BookingStatusInfo status;
        private Instant changedAt;
        private UserInfo changedBy;
        private String comment;
    }
    
    @Data
    public static class UserInfo {
        private Long id;
        private String email;
    }
    
    @Data
    public static class PreOrderItem {
        private Long id;
        private Long menuItemId;
        private MenuItemInfo menuItem;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal totalPrice;
        private String specialRequests;
    }
    
    @Data
    public static class MenuItemInfo {
        private Long id;
        private String name;
        private BigDecimal price;
    }
}

