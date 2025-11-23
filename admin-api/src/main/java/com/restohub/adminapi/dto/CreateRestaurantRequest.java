package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.Instagram;
import com.restohub.adminapi.validation.Phone;
import com.restohub.adminapi.validation.ValidImageId;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRestaurantRequest {
    
    @NotBlank(message = "Название ресторана обязательно")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String name;
    
    @NotBlank(message = "Адрес обязателен")
    @Size(max = 500, message = "Адрес не должен превышать 500 символов")
    private String address;
    
    @NotBlank(message = "Телефон обязателен")
    @Size(max = 50, message = "Телефон не должен превышать 50 символов")
    @Phone
    private String phone;
    
    @Size(max = 50, message = "WhatsApp не должен превышать 50 символов")
    @Phone
    private String whatsapp;
    
    @Size(max = 255, message = "Instagram не должен превышать 255 символов")
    @Instagram
    private String instagram;
    
    @Size(max = 10000, message = "Описание не должно превышать 10000 символов")
    private String description;
    
    @DecimalMin(value = "-90.0", message = "Широта должна быть от -90 до 90")
    @DecimalMax(value = "90.0", message = "Широта должна быть от -90 до 90")
    private BigDecimal latitude;
    
    @DecimalMin(value = "-180.0", message = "Долгота должна быть от -180 до 180")
    @DecimalMax(value = "180.0", message = "Долгота должна быть от -180 до 180")
    private BigDecimal longitude;
    
    @Size(max = 1000, message = "Рабочие часы не должны превышать 1000 символов")
    private String workingHours;
    
    @Size(max = 10, message = "Код языка не должен превышать 10 символов")
    private String managerLanguageCode;
    
    @ValidImageId
    private Long logoImageId;
    
    @ValidImageId
    private Long bgImageId;
    
    private Long userId;
}

