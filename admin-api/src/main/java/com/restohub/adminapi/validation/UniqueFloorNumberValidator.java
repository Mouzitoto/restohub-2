package com.restohub.adminapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class UniqueFloorNumberValidator implements ConstraintValidator<UniqueFloorNumber, String> {
    
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
    }
}

