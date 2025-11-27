package com.restohub.clientapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableMapResponse {
    private List<FloorMapResponse> floors;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FloorMapResponse {
        private Long id;
        private String floorNumber;
        private List<RoomMapResponse> rooms;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomMapResponse {
        private Long id;
        private String name;
        private Long imageId;
        private List<TableMapItemResponse> tables;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableMapItemResponse {
        private Long id;
        private String tableNumber;
        private Integer capacity;
        private java.math.BigDecimal positionX1;
        private java.math.BigDecimal positionY1;
        private java.math.BigDecimal positionX2;
        private java.math.BigDecimal positionY2;
    }
}
