package com.restohub.adminapi.validation;

import com.restohub.adminapi.repository.MenuCategoryRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueCategoryNameValidator implements ConstraintValidator<UniqueCategoryName, String> {
    
    @Autowired
    private MenuCategoryRepository menuCategoryRepository;
    
    @Override
    public void initialize(UniqueCategoryName constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.trim().isEmpty()) {
            return true; // null проверяется через @NotNull
        }
        
        return menuCategoryRepository.findByNameAndIsActiveTrue(name.trim()).isEmpty();
    }
}

