package com.restohub.adminapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivateSubscriptionRequest {
    
    @NotBlank(message = "Номер подписки обязателен")
    private String paymentReference;
    
    @NotNull(message = "Сумма платежа обязательна")
    private BigDecimal amount;
    
    @NotNull(message = "Дата платежа обязательна")
    private LocalDateTime paymentDate;
    
    @NotBlank(message = "ID транзакции обязателен")
    private String externalTransactionId;
}

