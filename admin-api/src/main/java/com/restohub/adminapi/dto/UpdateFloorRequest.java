package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.UniqueFloorNumber;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFloorRequest {
    
    @Size(max = 50, message = "Номер этажа не должен превышать 50 символов")
    @UniqueFloorNumber
    private String floorNumber;
}

