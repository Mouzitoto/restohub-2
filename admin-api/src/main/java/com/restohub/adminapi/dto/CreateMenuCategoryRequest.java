package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.UniqueCategoryName;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateMenuCategoryRequest {
    
    @NotBlank(message = "Название категории обязательно")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    @UniqueCategoryName
    private String name;
    
    @Size(max = 10000, message = "Описание не должно превышать 10000 символов")
    private String description;
    
    @Min(value = 0, message = "Порядок отображения должен быть не менее 0")
    private Integer displayOrder;
}

