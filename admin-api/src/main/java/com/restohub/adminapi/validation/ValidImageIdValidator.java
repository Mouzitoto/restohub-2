package com.restohub.adminapi.validation;

import com.restohub.adminapi.repository.ImageRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidImageIdValidator implements ConstraintValidator<ValidImageId, Long> {
    
    @Autowired
    private ImageRepository imageRepository;
    
    @Override
    public void initialize(ValidImageId constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(Long imageId, ConstraintValidatorContext context) {
        if (imageId == null) {
            return true; // null проверяется через @NotNull или nullable
        }
        
        return imageRepository.findByIdAndIsActiveTrue(imageId).isPresent();
    }
}

