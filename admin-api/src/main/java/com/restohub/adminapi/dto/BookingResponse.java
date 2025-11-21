package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class BookingResponse {
    private Long id;
    private Long restaurantId;
    private Long tableId;
    private TableInfo table;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private Integer personCount;
    private String clientName;
    private Long clientId;
    private String specialRequests;
    private BookingStatusInfo status;
    private List<BookingHistoryItem> history;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    public static class TableInfo {
        private Long id;
        private String tableNumber;
        private Integer capacity;
        private RoomInfo room;
    }
    
    @Data
    public static class RoomInfo {
        private Long id;
        private String name;
        private FloorInfo floor;
    }
    
    @Data
    public static class FloorInfo {
        private Long id;
        private String floorNumber;
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
}

