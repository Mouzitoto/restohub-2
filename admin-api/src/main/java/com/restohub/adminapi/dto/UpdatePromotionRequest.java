package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.ValidImageId;
import com.restohub.adminapi.validation.ValidPromotionTypeId;
import com.restohub.adminapi.validation.ValidRecurrenceType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdatePromotionRequest {
    
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String title;
    
    @Size(max = 10000, message = "Описание не должно превышать 10000 символов")
    private String description;
    
    @ValidPromotionTypeId
    private Long promotionTypeId;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @ValidImageId
    private Long imageId;
    
    private Boolean isRecurring;
    
    @ValidRecurrenceType
    private String recurrenceType;
    
    private List<@Min(value = 1, message = "День недели должен быть от 1 до 7") @Max(value = 7, message = "День недели должен быть от 1 до 7") Integer> recurrenceDaysOfWeek;
}

