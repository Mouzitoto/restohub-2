package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.ValidSubscriptionTypeId;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateSubscriptionRequest {
    
    @ValidSubscriptionTypeId
    private Long subscriptionTypeId;
    
    private LocalDate startDate;
    
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDate endDate;
    
    private Boolean isActive;
    
    @Size(max = 10000, message = "Описание не должно превышать 10000 символов")
    private String description;
}

