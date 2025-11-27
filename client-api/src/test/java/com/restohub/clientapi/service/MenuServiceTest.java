package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.MenuResponse;
import com.restohub.clientapi.entity.MenuCategory;
import com.restohub.clientapi.entity.MenuItem;
import com.restohub.clientapi.entity.Restaurant;
import com.restohub.clientapi.repository.MenuCategoryRepository;
import com.restohub.clientapi.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {
    
    @Mock
    private MenuItemRepository menuItemRepository;
    
    @Mock
    private MenuCategoryRepository menuCategoryRepository;
    
    @InjectMocks
    private MenuService menuService;
    
    private MenuCategory category;
    private MenuItem menuItem;
    
    @BeforeEach
    void setUp() {
        category = new MenuCategory();
        category.setId(1L);
        category.setName("Main Course");
        category.setDisplayOrder(1);
        
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        
        menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setRestaurant(restaurant);
        menuItem.setMenuCategory(category);
        menuItem.setName("Test Dish");
        menuItem.setPrice(new BigDecimal("500.00"));
        menuItem.setIsActive(true);
        menuItem.setIsAvailable(true);
        menuItem.setDisplayOrder(1);
    }
    
    @Test
    void testGetMenuByRestaurantId() {
        // Given
        List<MenuItem> items = Arrays.asList(menuItem);
        List<MenuCategory> categories = Arrays.asList(category);
        
        when(menuItemRepository.findByRestaurantIdAndIsActiveTrueAndIsAvailableTrue(1L))
                .thenReturn(items);
        when(menuCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(categories);
        
        // When
        MenuResponse result = menuService.getMenuByRestaurantId(1L);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getCategories());
        assertEquals(1, result.getCategories().size());
        assertEquals(category.getId(), result.getCategories().get(0).getId());
        verify(menuItemRepository).findByRestaurantIdAndIsActiveTrueAndIsAvailableTrue(1L);
    }
}

