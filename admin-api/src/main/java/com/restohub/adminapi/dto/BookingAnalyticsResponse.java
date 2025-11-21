package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class BookingAnalyticsResponse {
    private Long restaurantId;
    private Period period;
    private Summary summary;
    private List<ChartItem> chart;
    private List<PopularTable> popularTables;
    
    @Data
    public static class Period {
        private LocalDate dateFrom;
        private LocalDate dateTo;
    }
    
    @Data
    public static class Summary {
        private Long total;
        private Map<String, Long> byStatus;
        private Double averagePersons;
        private Double conversionRate;
    }
    
    @Data
    public static class ChartItem {
        private String period;
        private Long count;
        private Map<String, Long> byStatus;
    }
    
    @Data
    public static class PopularTable {
        private Long tableId;
        private String tableNumber;
        private Long count;
        private Double percentage;
    }
}

