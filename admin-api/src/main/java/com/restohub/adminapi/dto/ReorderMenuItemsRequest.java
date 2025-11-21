package com.restohub.adminapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReorderMenuItemsRequest {
    
    @NotNull(message = "Список блюд обязателен")
    @NotEmpty(message = "Список блюд не может быть пустым")
    @Valid
    private List<MenuItemOrder> items;
    
    @Data
    public static class MenuItemOrder {
        @NotNull(message = "ID блюда обязателен")
        private Long id;
        
        @NotNull(message = "Порядок отображения обязателен")
        @jakarta.validation.constraints.Min(value = 0, message = "Порядок отображения должен быть не менее 0")
        private Integer displayOrder;
    }
}

