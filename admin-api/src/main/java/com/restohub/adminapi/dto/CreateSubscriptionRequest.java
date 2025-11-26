package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.ValidSubscriptionTypeId;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {
    
    @NotNull(message = "Тип подписки обязателен")
    @ValidSubscriptionTypeId
    private Long subscriptionTypeId;
}

