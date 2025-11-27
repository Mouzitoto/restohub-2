package com.restohub.clientapi.validation;

import com.restohub.clientapi.entity.Restaurant;
import com.restohub.clientapi.repository.RestaurantRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidRestaurantIdValidatorTest {
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private ConstraintValidatorContext context;
    
    @InjectMocks
    private ValidRestaurantIdValidator validator;
    
    @BeforeEach
    void setUp() throws Exception {
        validator = new ValidRestaurantIdValidator();
        // Устанавливаем restaurantRepository через рефлексию
        Field field = ValidRestaurantIdValidator.class.getDeclaredField("restaurantRepository");
        field.setAccessible(true);
        field.set(validator, restaurantRepository);
    }
    
    @Test
    void testValidRestaurantId() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setIsActive(true);
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(restaurant));
        
        // When
        boolean result = validator.isValid(1L, context);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testInvalidRestaurantIdNotFound() {
        // Given
        when(restaurantRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());
        
        // When
        boolean result = validator.isValid(1L, context);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testInvalidRestaurantIdNull() {
        // When
        boolean result = validator.isValid(null, context);
        
        // Then
        assertFalse(result);
    }
}

