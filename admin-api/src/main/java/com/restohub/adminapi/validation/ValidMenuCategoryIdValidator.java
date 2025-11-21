package com.restohub.adminapi.validation;

import com.restohub.adminapi.repository.MenuCategoryRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidMenuCategoryIdValidator implements ConstraintValidator<ValidMenuCategoryId, Long> {
    
    @Autowired
    private MenuCategoryRepository menuCategoryRepository;
    
    @Override
    public void initialize(ValidMenuCategoryId constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(Long categoryId, ConstraintValidatorContext context) {
        if (categoryId == null) {
            return true; // null проверяется через @NotNull
        }
        
        return menuCategoryRepository.findByIdAndIsActiveTrue(categoryId).isPresent();
    }
}

