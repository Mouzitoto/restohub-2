package com.restohub.clientapi.validation;

import com.restohub.clientapi.repository.RestaurantRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidRestaurantIdValidator implements ConstraintValidator<ValidRestaurantId, Long> {
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Override
    public void initialize(ValidRestaurantId constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(Long restaurantId, ConstraintValidatorContext context) {
        if (restaurantId == null) {
            return false;
        }
        return restaurantRepository.findByIdAndIsActiveTrue(restaurantId).isPresent();
    }
}

