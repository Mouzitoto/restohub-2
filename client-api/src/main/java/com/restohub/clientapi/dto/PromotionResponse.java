package com.restohub.clientapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {
    private Long id;
    private String title;
    private String description;
    private PromotionTypeResponse promotionType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long imageId;
    private Boolean isRecurring;
    private String recurrenceType;
    private List<Integer> recurrenceDayOfWeek;
}

