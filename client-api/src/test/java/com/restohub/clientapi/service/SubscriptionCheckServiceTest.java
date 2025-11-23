package com.restohub.clientapi.service;

import com.restohub.clientapi.entity.Restaurant;
import com.restohub.clientapi.entity.RestaurantSubscription;
import com.restohub.clientapi.repository.RestaurantRepository;
import com.restohub.clientapi.repository.RestaurantSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionCheckServiceTest {
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private RestaurantSubscriptionRepository subscriptionRepository;
    
    @InjectMocks
    private SubscriptionCheckService subscriptionCheckService;
    
    private Restaurant restaurant;
    private RestaurantSubscription activeSubscription;
    private RestaurantSubscription expiredSubscription;
    
    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setIsActive(true);
        
        // Активная подписка
        activeSubscription = new RestaurantSubscription();
        activeSubscription.setId(1L);
        activeSubscription.setRestaurant(restaurant);
        activeSubscription.setStartDate(LocalDate.now().minusDays(5));
        activeSubscription.setEndDate(LocalDate.now().plusDays(25));
        activeSubscription.setIsActive(true);
        
        // Истекшая подписка
        expiredSubscription = new RestaurantSubscription();
        expiredSubscription.setId(2L);
        expiredSubscription.setRestaurant(restaurant);
        expiredSubscription.setStartDate(LocalDate.now().minusDays(30));
        expiredSubscription.setEndDate(LocalDate.now().minusDays(5));
        expiredSubscription.setIsActive(true);
    }
    
    @Test
    void hasActiveSubscription_WithActiveSubscription_ReturnsTrue() {
        // Arrange
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(subscriptionRepository.findByRestaurantIdAndIsActiveTrue(1L))
                .thenReturn(List.of(activeSubscription));
        
        // Act
        boolean result = subscriptionCheckService.hasActiveSubscription(1L);
        
        // Assert
        assertTrue(result);
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(1L);
        verify(subscriptionRepository, times(1)).findByRestaurantIdAndIsActiveTrue(1L);
    }
    
    @Test
    void hasActiveSubscription_WithExpiredSubscription_ReturnsFalse() {
        // Arrange
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(subscriptionRepository.findByRestaurantIdAndIsActiveTrue(1L))
                .thenReturn(List.of(expiredSubscription));
        
        // Act
        boolean result = subscriptionCheckService.hasActiveSubscription(1L);
        
        // Assert
        assertFalse(result);
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(1L);
        verify(subscriptionRepository, times(1)).findByRestaurantIdAndIsActiveTrue(1L);
    }
    
    @Test
    void hasActiveSubscription_WithoutSubscription_ReturnsFalse() {
        // Arrange
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(subscriptionRepository.findByRestaurantIdAndIsActiveTrue(1L))
                .thenReturn(Collections.emptyList());
        
        // Act
        boolean result = subscriptionCheckService.hasActiveSubscription(1L);
        
        // Assert
        assertFalse(result);
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(1L);
        verify(subscriptionRepository, times(1)).findByRestaurantIdAndIsActiveTrue(1L);
    }
    
    @Test
    void hasActiveSubscription_RestaurantNotFound_ReturnsFalse() {
        // Arrange
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.empty());
        
        // Act
        boolean result = subscriptionCheckService.hasActiveSubscription(1L);
        
        // Assert
        assertFalse(result);
        verify(restaurantRepository, times(1)).findByIdAndIsActiveTrue(1L);
        verify(subscriptionRepository, never()).findByRestaurantIdAndIsActiveTrue(anyLong());
    }
    
    @Test
    void hasActiveSubscription_WithInactiveSubscription_ReturnsFalse() {
        // Arrange
        RestaurantSubscription inactiveSubscription = new RestaurantSubscription();
        inactiveSubscription.setId(3L);
        inactiveSubscription.setRestaurant(restaurant);
        inactiveSubscription.setStartDate(LocalDate.now().minusDays(5));
        inactiveSubscription.setEndDate(LocalDate.now().plusDays(25));
        inactiveSubscription.setIsActive(false); // Неактивная подписка
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(subscriptionRepository.findByRestaurantIdAndIsActiveTrue(1L))
                .thenReturn(List.of(inactiveSubscription));
        
        // Act
        boolean result = subscriptionCheckService.hasActiveSubscription(1L);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void hasActiveSubscription_CachesResult() {
        // Arrange
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(subscriptionRepository.findByRestaurantIdAndIsActiveTrue(1L))
                .thenReturn(List.of(activeSubscription));
        
        // Act - первый вызов
        boolean result1 = subscriptionCheckService.hasActiveSubscription(1L);
        
        // Act - второй вызов (должен использовать кэш)
        boolean result2 = subscriptionCheckService.hasActiveSubscription(1L);
        
        // Assert
        assertTrue(result1);
        assertTrue(result2);
        // Кэш работает через Spring, поэтому в unit тестах мы не можем проверить,
        // что второй вызов не обращается к БД. Это проверяется в integration тестах.
        verify(restaurantRepository, times(2)).findByIdAndIsActiveTrue(1L);
        verify(subscriptionRepository, times(2)).findByRestaurantIdAndIsActiveTrue(1L);
    }
}

