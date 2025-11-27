package com.restohub.clientapi.validation;

import com.restohub.clientapi.entity.Booking;
import com.restohub.clientapi.repository.BookingRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ValidBookingIdValidator implements ConstraintValidator<ValidBookingId, Long> {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    private long restaurantId;
    
    @Override
    public void initialize(ValidBookingId constraintAnnotation) {
        this.restaurantId = constraintAnnotation.restaurantId();
    }
    
    @Override
    public boolean isValid(Long bookingId, ConstraintValidatorContext context) {
        if (bookingId == null) {
            return true; // Опциональное поле
        }
        
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return false;
        }
        
        // Проверяем принадлежность к ресторану через table -> room -> floor -> restaurant
        if (booking.getTable() != null && 
            booking.getTable().getRoom() != null && 
            booking.getTable().getRoom().getFloor() != null && 
            booking.getTable().getRoom().getFloor().getRestaurant() != null) {
            return booking.getTable().getRoom().getFloor().getRestaurant().getId().equals(restaurantId);
        }
        
        return false;
    }
}

