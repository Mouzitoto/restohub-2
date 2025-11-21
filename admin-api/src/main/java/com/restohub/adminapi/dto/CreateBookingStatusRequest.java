package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.UniqueBookingStatusCode;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateBookingStatusRequest {
    
    @NotBlank(message = "Код статуса обязателен")
    @Size(max = 50, message = "Код статуса не должен превышать 50 символов")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Код статуса должен содержать только латинские буквы (верхний регистр), цифры и подчеркивания")
    @UniqueBookingStatusCode
    private String code;
    
    @NotBlank(message = "Название статуса обязательно")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String name;
    
    @Min(value = 0, message = "Порядок отображения должен быть не менее 0")
    private Integer displayOrder;
}

