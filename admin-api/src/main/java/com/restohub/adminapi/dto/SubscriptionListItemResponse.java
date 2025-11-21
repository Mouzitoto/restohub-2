package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SubscriptionListItemResponse {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private SubscriptionTypeInfo subscriptionType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private Integer daysRemaining;
    private Boolean isExpiringSoon;
    
    @Data
    public static class SubscriptionTypeInfo {
        private Long id;
        private String code;
        private String name;
    }
}

