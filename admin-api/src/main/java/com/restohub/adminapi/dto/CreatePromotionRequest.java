package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.ValidImageId;
import com.restohub.adminapi.validation.ValidPromotionTypeId;
import com.restohub.adminapi.validation.ValidRecurrenceType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreatePromotionRequest {
    
    @NotBlank(message = "Название акции обязательно")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String title;
    
    @Size(max = 10000, message = "Описание не должно превышать 10000 символов")
    private String description;
    
    @NotNull(message = "ID типа промо-события обязателен")
    @ValidPromotionTypeId
    private Long promotionTypeId;
    
    @NotNull(message = "Дата начала обязательна")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @ValidImageId
    private Long imageId;
    
    private Boolean isRecurring;
    
    @ValidRecurrenceType
    private String recurrenceType;
    
    @Min(value = 1, message = "День недели должен быть от 1 до 7")
    @Max(value = 7, message = "День недели должен быть от 1 до 7")
    private Integer recurrenceDayOfWeek;
}

