package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.UserInfoResponse;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import com.restohub.adminapi.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private UserRestaurantRepository userRestaurantRepository;
    
    @Mock
    private RestaurantSubscriptionRepository restaurantSubscriptionRepository;
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @InjectMocks
    private AuthenticationService authenticationService;
    
    private User adminUser;
    private User managerUser;
    private Role adminRole;
    private Role managerRole;
    private Restaurant restaurant1;
    private Restaurant restaurant2;
    
    @BeforeEach
    void setUp() {
        // Настройка ролей
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setCode("ADMIN");
        adminRole.setName("Администратор");
        
        managerRole = new Role();
        managerRole.setId(2L);
        managerRole.setCode("MANAGER");
        managerRole.setName("Менеджер");
        
        // Настройка админа
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(adminRole);
        adminUser.setIsActive(true);
        
        // Настройка менеджера
        managerUser = new User();
        managerUser.setId(2L);
        managerUser.setEmail("manager@test.com");
        managerUser.setRole(managerRole);
        managerUser.setIsActive(true);
        
        // Настройка ресторанов
        restaurant1 = new Restaurant();
        restaurant1.setId(1L);
        restaurant1.setName("Ресторан 1");
        restaurant1.setIsActive(true);
        
        restaurant2 = new Restaurant();
        restaurant2.setId(2L);
        restaurant2.setName("Ресторан 2");
        restaurant2.setIsActive(true);
    }
    
    @Test
    void testGetCurrentUser_Admin_ReturnsAllRestaurants() {
        // Arrange
        String email = "admin@test.com";
        String role = "ADMIN";
        
        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(adminUser));
        when(restaurantRepository.findAll()).thenReturn(Arrays.asList(restaurant1, restaurant2));
        when(restaurantSubscriptionRepository.findByRestaurantIdAndIsActiveTrue(anyLong()))
                .thenReturn(new ArrayList<>());
        
        // Act
        UserInfoResponse response = authenticationService.getCurrentUser(email, role);
        
        // Assert
        assertNotNull(response);
        assertEquals(email, response.getEmail());
        assertEquals(role, response.getRole());
        assertNotNull(response.getRestaurants());
        assertEquals(2, response.getRestaurants().size());
        assertEquals("Ресторан 1", response.getRestaurants().get(0).getName());
        assertEquals("Ресторан 2", response.getRestaurants().get(1).getName());
        
        verify(restaurantRepository, times(1)).findAll();
        verify(userRestaurantRepository, never()).findByUserId(anyLong());
    }
    
    @Test
    void testGetCurrentUser_Manager_ReturnsManagerRestaurants() {
        // Arrange
        String email = "manager@test.com";
        String role = "MANAGER";
        
        UserRestaurant userRestaurant = new UserRestaurant();
        userRestaurant.setUser(managerUser);
        userRestaurant.setRestaurant(restaurant1);
        
        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(managerUser));
        when(userRestaurantRepository.findByUserId(managerUser.getId()))
                .thenReturn(Arrays.asList(userRestaurant));
        when(restaurantSubscriptionRepository.findByRestaurantIdAndIsActiveTrue(anyLong()))
                .thenReturn(new ArrayList<>());
        
        // Act
        UserInfoResponse response = authenticationService.getCurrentUser(email, role);
        
        // Assert
        assertNotNull(response);
        assertEquals(email, response.getEmail());
        assertEquals(role, response.getRole());
        assertNotNull(response.getRestaurants());
        assertEquals(1, response.getRestaurants().size());
        assertEquals("Ресторан 1", response.getRestaurants().get(0).getName());
        
        verify(restaurantRepository, never()).findAll();
        verify(userRestaurantRepository, times(1)).findByUserId(managerUser.getId());
    }
    
    @Test
    void testGetCurrentUser_Admin_WithSubscriptions() {
        // Arrange
        String email = "admin@test.com";
        String role = "ADMIN";
        
        RestaurantSubscription subscription = new RestaurantSubscription();
        subscription.setRestaurant(restaurant1);
        subscription.setIsActive(true);
        subscription.setEndDate(LocalDate.now().plusDays(10));
        
        when(userRepository.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(adminUser));
        when(restaurantRepository.findAll()).thenReturn(Arrays.asList(restaurant1));
        when(restaurantSubscriptionRepository.findByRestaurantIdAndIsActiveTrue(restaurant1.getId()))
                .thenReturn(Arrays.asList(subscription));
        
        // Act
        UserInfoResponse response = authenticationService.getCurrentUser(email, role);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getRestaurants().size());
        assertNotNull(response.getRestaurants().get(0).getSubscription());
        assertEquals(restaurant1.getId(), response.getRestaurants().get(0).getSubscription().getRestaurantId());
        assertTrue(response.getRestaurants().get(0).getSubscription().getIsActive());
    }
}

