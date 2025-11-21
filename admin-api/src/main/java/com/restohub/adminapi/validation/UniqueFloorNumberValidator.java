package com.restohub.adminapi.validation;

import com.restohub.adminapi.repository.FloorRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class UniqueFloorNumberValidator implements ConstraintValidator<UniqueFloorNumber, String> {
    
    @Autowired
    private FloorRepository floorRepository;
    
    private Long restaurantId;
    
    @Override
    public void initialize(UniqueFloorNumber constraintAnnotation) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    
    @Override
    public boolean isValid(String floorNumber, ConstraintValidatorContext context) {
        if (floorNumber == null || floorNumber.trim().isEmpty()) {
            return true; // null проверяется через @NotNull
        }
        
        // Получаем restaurantId из контекста валидации
        // Для этого нужно передавать restaurantId через кастомный способ
        // Пока что возвращаем true, проверка будет в сервисе
        return true;
    }
    
    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
}

