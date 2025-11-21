package com.restohub.adminapi.dto;

import com.restohub.adminapi.validation.ValidImageId;
import com.restohub.adminapi.validation.ValidMenuCategoryId;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateMenuItemRequest {
    
    @NotBlank(message = "Название блюда обязательно")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String name;
    
    @Size(max = 10000, message = "Описание не должно превышать 10000 символов")
    private String description;
    
    @Size(max = 10000, message = "Ингредиенты не должны превышать 10000 символов")
    private String ingredients;
    
    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.01", message = "Цена должна быть не менее 0.01")
    @Digits(integer = 8, fraction = 2, message = "Цена должна иметь не более 8 цифр до запятой и 2 после")
    private BigDecimal price;
    
    @NotNull(message = "ID категории меню обязателен")
    @ValidMenuCategoryId
    private Long menuCategoryId;
    
    @Min(value = 0, message = "Процент скидки должен быть от 0 до 100")
    @Max(value = 100, message = "Процент скидки должен быть от 0 до 100")
    private Integer discountPercent;
    
    @Min(value = 0, message = "Уровень остроты должен быть от 0 до 5")
    @Max(value = 5, message = "Уровень остроты должен быть от 0 до 5")
    private Integer spicinessLevel;
    
    private Boolean hasSugar;
    
    @ValidImageId
    private Long imageId;
    
    @Min(value = 0, message = "Порядок отображения должен быть не менее 0")
    private Integer displayOrder;
}

