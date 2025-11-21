package com.restohub.adminapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AnalyticsOverviewResponse {
    private Long restaurantId;
    private Period period;
    private BookingsInfo bookings;
    private PreOrdersInfo preOrders;
    private ClientsInfo clients;
    private List<PopularItem> popularItems;
    private List<PopularTable> popularTables;
    
    @Data
    public static class Period {
        private LocalDate dateFrom;
        private LocalDate dateTo;
    }
    
    @Data
    public static class BookingsInfo {
        private Long total;
        private Long approved;
        private Double conversionRate;
    }
    
    @Data
    public static class PreOrdersInfo {
        private Long total;
        private Long approved;
        private BigDecimal totalRevenue;
        private BigDecimal averageCheck;
        private Double conversionRate;
    }
    
    @Data
    public static class ClientsInfo {
        private Long total;
        private Long newClients;
        private Long returningClients;
    }
    
    @Data
    public static class PopularItem {
        private Long menuItemId;
        private String menuItemName;
        private Long quantity;
    }
    
    @Data
    public static class PopularTable {
        private Long tableId;
        private String tableNumber;
        private Long count;
    }
}

