package com.restohub.adminapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ClientAnalyticsResponse {
    private Long restaurantId;
    private Period period;
    private Summary summary;
    private List<TopClient> topClients;
    
    @Data
    public static class Period {
        private LocalDate dateFrom;
        private LocalDate dateTo;
    }
    
    @Data
    public static class Summary {
        private Long total;
        private Long newClients;
        private Long returningClients;
        private Double averageBookingsPerClient;
        private Double averagePreOrdersPerClient;
    }
    
    @Data
    public static class TopClient {
        private Long clientId;
        private String clientPhone;
        private Integer totalBookings;
        private Integer totalPreOrders;
        private BigDecimal totalSpent;
    }
}

