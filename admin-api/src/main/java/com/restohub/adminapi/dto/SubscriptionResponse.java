package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class SubscriptionResponse {
    private Long id;
    private Long restaurantId;
    private SubscriptionTypeInfo subscriptionType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private String description;
    private Integer daysRemaining;
    private Boolean isExpiringSoon;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    public static class SubscriptionTypeInfo {
        private Long id;
        private String code;
        private String name;
        private String description;
        private java.math.BigDecimal price;
    }
}

