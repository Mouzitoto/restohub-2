package com.restohub.adminapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class TableMapResponse {
    private List<FloorMapItem> floors;
    
    @Data
    public static class FloorMapItem {
        private Long id;
        private String floorNumber;
        private List<RoomMapItem> rooms;
    }
    
    @Data
    public static class RoomMapItem {
        private Long id;
        private String name;
        private Long imageId;
        private List<TableMapItem> tables;
    }
    
    @Data
    public static class TableMapItem {
        private Long id;
        private String tableNumber;
        private Integer capacity;
        private String description;
        private Long imageId;
        private String depositAmount;
        private String depositNote;
    }
}

