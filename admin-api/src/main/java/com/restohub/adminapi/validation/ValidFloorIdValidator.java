package com.restohub.adminapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class ValidFloorIdValidator implements ConstraintValidator<ValidFloorId, Long> {
    
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
}

