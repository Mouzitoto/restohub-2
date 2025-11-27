package com.restohub.clientapi.validation;

import com.restohub.clientapi.entity.RestaurantTable;
import com.restohub.clientapi.repository.TableRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidTableIdValidator implements ConstraintValidator<ValidTableId, Long> {
    
    @Autowired
    private TableRepository tableRepository;
    
    private long restaurantId;
    
    @Override
    public void initialize(ValidTableId constraintAnnotation) {
        this.restaurantId = constraintAnnotation.restaurantId();
    }
    
    @Override
    public boolean isValid(Long tableId, ConstraintValidatorContext context) {
        if (tableId == null) {
            return false;
        }
        
        RestaurantTable table = tableRepository.findByIdAndIsActiveTrue(tableId).orElse(null);
        if (table == null) {
            return false;
        }
        
        // Проверяем принадлежность к ресторану через room -> floor -> restaurant
        if (table.getRoom() != null && 
            table.getRoom().getFloor() != null && 
            table.getRoom().getFloor().getRestaurant() != null) {
            return table.getRoom().getFloor().getRestaurant().getId().equals(restaurantId);
        }
        
        return false;
    }
}

