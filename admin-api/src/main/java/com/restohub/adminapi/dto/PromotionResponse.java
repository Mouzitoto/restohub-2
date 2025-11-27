package com.restohub.adminapi.dto;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class PromotionResponse {
    private Long id;
    private Long restaurantId;
    private PromotionTypeInfo promotionType;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long imageId;
    private Boolean isRecurring;
    private String recurrenceType;
    private List<Integer> recurrenceDaysOfWeek;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    
    @Data
    public static class PromotionTypeInfo {
        private Long id;
        private String code;
        private String name;
    }
}

