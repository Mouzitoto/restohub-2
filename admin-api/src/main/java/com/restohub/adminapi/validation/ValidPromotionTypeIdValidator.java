package com.restohub.adminapi.validation;

import com.restohub.adminapi.repository.PromotionTypeRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidPromotionTypeIdValidator implements ConstraintValidator<ValidPromotionTypeId, Long> {
    
    @Autowired
    private PromotionTypeRepository promotionTypeRepository;
    
    @Override
    public void initialize(ValidPromotionTypeId constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(Long promotionTypeId, ConstraintValidatorContext context) {
        if (promotionTypeId == null) {
            return true; // null проверяется через @NotNull
        }
        
        return promotionTypeRepository.findByIdAndIsActiveTrue(promotionTypeId).isPresent();
    }
}

