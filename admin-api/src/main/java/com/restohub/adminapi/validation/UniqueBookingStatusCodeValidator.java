package com.restohub.adminapi.validation;

import com.restohub.adminapi.repository.BookingStatusRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueBookingStatusCodeValidator implements ConstraintValidator<UniqueBookingStatusCode, String> {
    
    @Autowired
    private BookingStatusRepository bookingStatusRepository;
    
    @Override
    public void initialize(UniqueBookingStatusCode constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String code, ConstraintValidatorContext context) {
        if (code == null || code.trim().isEmpty()) {
            return true; // null проверяется через @NotNull
        }
        
        return bookingStatusRepository.findByCodeAndIsActiveTrue(code.trim().toUpperCase()).isEmpty();
    }
}

