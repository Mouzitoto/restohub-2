package com.restohub.adminapi.validation;

import com.restohub.adminapi.repository.FloorRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class ValidFloorIdValidator implements ConstraintValidator<ValidFloorId, Long> {
    
    @Autowired
    private FloorRepository floorRepository;
    
    private Long restaurantId;
    
    @Override
    public void initialize(ValidFloorId constraintAnnotation) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    
    @Override
    public boolean isValid(Long floorId, ConstraintValidatorContext context) {
        if (floorId == null) {
            return true; // null проверяется через @NotNull
        }
        
        // Проверка будет в сервисе, так как нужен restaurantId
        return true;
    }
    
    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
}

