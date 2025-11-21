package com.restohub.adminapi.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReorderMenuCategoriesRequest {
    
    @NotNull(message = "Список ID категорий обязателен")
    @NotEmpty(message = "Список ID категорий не может быть пустым")
    private List<Long> categoryIds;
}

