package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.UniqueTableNumber;
import com.restohub.adminapi.validation.ValidImageId;
import com.restohub.adminapi.validation.ValidRoomId;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTableRequest {
    
    @Size(max = 50, message = "Номер стола не должен превышать 50 символов")
    @UniqueTableNumber
    private String tableNumber;
    
    @ValidRoomId
    private Long roomId;
    
    @Min(value = 1, message = "Количество мест должно быть от 1 до 100")
    @Max(value = 100, message = "Количество мест должно быть от 1 до 100")
    private Integer capacity;
    
    @Size(max = 10000, message = "Описание не должно превышать 10000 символов")
    private String description;
    
    @ValidImageId
    private Long imageId;
    
    @Size(max = 255, message = "Минимальная сумма депозита не должна превышать 255 символов")
    private String depositAmount;
    
    @Size(max = 1000, message = "Примечание о депозите не должно превышать 1000 символов")
    private String depositNote;
    
    @DecimalMin(value = "0.0", message = "Координата X1 должна быть от 0 до 100")
    @DecimalMax(value = "100.0", message = "Координата X1 должна быть от 0 до 100")
    private BigDecimal positionX1;
    
    @DecimalMin(value = "0.0", message = "Координата Y1 должна быть от 0 до 100")
    @DecimalMax(value = "100.0", message = "Координата Y1 должна быть от 0 до 100")
    private BigDecimal positionY1;
    
    @DecimalMin(value = "0.0", message = "Координата X2 должна быть от 0 до 100")
    @DecimalMax(value = "100.0", message = "Координата X2 должна быть от 0 до 100")
    private BigDecimal positionX2;
    
    @DecimalMin(value = "0.0", message = "Координата Y2 должна быть от 0 до 100")
    @DecimalMax(value = "100.0", message = "Координата Y2 должна быть от 0 до 100")
    private BigDecimal positionY2;
}

