package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.ValidFloorId;
import com.restohub.adminapi.validation.ValidImageId;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRoomRequest {
    
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String name;
    
    @Size(max = 10000, message = "Описание не должно превышать 10000 символов")
    private String description;
    
    @ValidFloorId
    private Long floorId;
    
    private Boolean isSmoking;
    
    private Boolean isOutdoor;
    
    private Boolean isLiveMusic;
    
    @ValidImageId
    private Long imageId;
}

