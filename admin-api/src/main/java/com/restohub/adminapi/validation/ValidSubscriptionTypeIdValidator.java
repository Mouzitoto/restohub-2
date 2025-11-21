package com.restohub.adminapi.validation;

import com.restohub.adminapi.repository.SubscriptionTypeRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidSubscriptionTypeIdValidator implements ConstraintValidator<ValidSubscriptionTypeId, Long> {
    
    @Autowired
    private SubscriptionTypeRepository subscriptionTypeRepository;
    
    @Override
    public void initialize(ValidSubscriptionTypeId constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(Long subscriptionTypeId, ConstraintValidatorContext context) {
        if (subscriptionTypeId == null) {
            return true; // null проверяется через @NotNull или nullable
        }
        
        return subscriptionTypeRepository.findByIdAndIsActiveTrue(subscriptionTypeId).isPresent();
    }
}

