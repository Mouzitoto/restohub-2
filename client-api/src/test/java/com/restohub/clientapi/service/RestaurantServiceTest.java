package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.RestaurantDetailResponse;
import com.restohub.clientapi.dto.RestaurantListResponse;
import com.restohub.clientapi.entity.Image;
import com.restohub.clientapi.entity.Restaurant;
import com.restohub.clientapi.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private SubscriptionCheckService subscriptionCheckService;
    
    @InjectMocks
    private RestaurantService restaurantService;
    
    private Restaurant restaurant;
    
    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setAddress("Test Address");
        restaurant.setPhone("+79991234567");
        restaurant.setLatitude(new BigDecimal("55.7558"));
        restaurant.setLongitude(new BigDecimal("37.6173"));
        restaurant.setDescription("Test Description");
        
        Image logo = new Image();
        logo.setId(10L);
        restaurant.setLogoImage(logo);
    }
    
    @Test
    void testGetRestaurants() {
        // Given
        List<Restaurant> restaurants = Arrays.asList(restaurant);
        when(restaurantRepository.findByIsActiveTrue()).thenReturn(restaurants);
        when(subscriptionCheckService.hasActiveSubscription(anyLong())).thenReturn(true);
        
        // When
        List<RestaurantListResponse> result = restaurantService.getRestaurants(10, 0);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(restaurant.getId(), result.get(0).getId());
        assertEquals(restaurant.getName(), result.get(0).getName());
        verify(restaurantRepository).findByIsActiveTrue();
    }
    
    @Test
    void testGetRestaurantById() {
        // Given
        when(restaurantRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(restaurant));
        
        // When
        RestaurantDetailResponse result = restaurantService.getRestaurantById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(restaurant.getId(), result.getId());
        assertEquals(restaurant.getName(), result.getName());
        verify(restaurantRepository).findByIdAndIsActiveTrue(1L);
    }
    
    @Test
    void testGetRestaurantByIdNotFound() {
        // Given
        when(restaurantRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(RuntimeException.class, () -> restaurantService.getRestaurantById(1L));
    }
}

