package com.restohub.adminapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class PreOrderAnalyticsResponse {
    private Long restaurantId;
    private Period period;
    private Summary summary;
    private List<ChartItem> chart;
    private List<PopularItem> popularItems;
    
    @Data
    public static class Period {
        private LocalDate dateFrom;
        private LocalDate dateTo;
    }
    
    @Data
    public static class Summary {
        private Long total;
        private Map<String, Long> byStatus;
        private BigDecimal totalRevenue;
        private BigDecimal averageCheck;
        private Double conversionRate;
    }
    
    @Data
    public static class ChartItem {
        private String period;
        private Long count;
        private BigDecimal revenue;
    }
    
    @Data
    public static class PopularItem {
        private Long menuItemId;
        private String menuItemName;
        private Long quantity;
        private BigDecimal revenue;
        private Double percentage;
    }
}

