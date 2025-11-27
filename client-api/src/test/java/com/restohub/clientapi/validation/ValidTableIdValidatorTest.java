package com.restohub.clientapi.validation;

import com.restohub.clientapi.entity.Floor;
import com.restohub.clientapi.entity.Restaurant;
import com.restohub.clientapi.entity.RestaurantTable;
import com.restohub.clientapi.entity.Room;
import com.restohub.clientapi.repository.TableRepository;
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
class ValidTableIdValidatorTest {
    
    @Mock
    private TableRepository tableRepository;
    
    @Mock
    private ConstraintValidatorContext context;
    
    @InjectMocks
    private ValidTableIdValidator validator;
    
    private Restaurant restaurant;
    private RestaurantTable table;
    
    @BeforeEach
    void setUp() throws Exception {
        validator = new ValidTableIdValidator();
        // Устанавливаем tableRepository через рефлексию
        Field field = ValidTableIdValidator.class.getDeclaredField("tableRepository");
        field.setAccessible(true);
        field.set(validator, tableRepository);
        
        restaurant = new Restaurant();
        restaurant.setId(1L);
        
        Floor floor = new Floor();
        floor.setRestaurant(restaurant);
        
        Room room = new Room();
        room.setFloor(floor);
        
        table = new RestaurantTable();
        table.setId(1L);
        table.setRoom(room);
        table.setIsActive(true);
    }
    
    @Test
    void testValidTableId() {
        // Given
        ValidTableId annotation = mock(ValidTableId.class);
        when(annotation.restaurantId()).thenReturn(1L);
        validator.initialize(annotation);
        
        when(tableRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(table));
        
        // When
        boolean result = validator.isValid(1L, context);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testInvalidTableIdNotFound() {
        // Given
        ValidTableId annotation = mock(ValidTableId.class);
        when(annotation.restaurantId()).thenReturn(1L);
        validator.initialize(annotation);
        
        when(tableRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());
        
        // When
        boolean result = validator.isValid(1L, context);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testInvalidTableIdNull() {
        // Given
        ValidTableId annotation = mock(ValidTableId.class);
        when(annotation.restaurantId()).thenReturn(1L);
        validator.initialize(annotation);
        
        // When
        boolean result = validator.isValid(null, context);
        
        // Then
        assertFalse(result);
    }
}

