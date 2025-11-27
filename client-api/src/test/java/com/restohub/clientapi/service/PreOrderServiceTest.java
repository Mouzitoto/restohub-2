package com.restohub.clientapi.service;

import com.restohub.clientapi.dto.CreatePreOrderRequest;
import com.restohub.clientapi.dto.CreatePreOrderResponse;
import com.restohub.clientapi.entity.*;
import com.restohub.clientapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreOrderServiceTest {
    
    @Mock
    private PreOrderRepository preOrderRepository;
    
    @Mock
    private PreOrderItemRepository preOrderItemRepository;
    
    @Mock
    private PreOrderHistoryRepository preOrderHistoryRepository;
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private MenuItemRepository menuItemRepository;
    
    @Mock
    private ClientRepository clientRepository;
    
    @Mock
    private BookingStatusRepository bookingStatusRepository;
    
    @InjectMocks
    private PreOrderService preOrderService;
    
    private Restaurant restaurant;
    private MenuItem menuItem;
    private BookingStatus pendingStatus;
    private CreatePreOrderRequest request;
    
    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setIsActive(true);
        
        menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setRestaurant(restaurant);
        menuItem.setPrice(new BigDecimal("500.00"));
        menuItem.setIsActive(true);
        menuItem.setIsAvailable(true);
        
        pendingStatus = new BookingStatus();
        pendingStatus.setId(2L);
        pendingStatus.setCode("PENDING");
        pendingStatus.setName("Ожидает подтверждения");
        
        CreatePreOrderRequest.PreOrderItemRequest itemRequest = 
                CreatePreOrderRequest.PreOrderItemRequest.builder()
                        .menuItemId(1L)
                        .quantity(2)
                        .build();
        
        request = CreatePreOrderRequest.builder()
                .restaurantId(1L)
                .date(LocalDate.now().plusDays(1).toString())
                .time("19:00:00")
                .clientPhone("+79991234567")
                .clientFirstName("Test")
                .items(new ArrayList<>())
                .build();
        request.getItems().add(itemRequest);
    }
    
    @Test
    void testCreatePreOrder() {
        // Given
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(menuItem));
        Client savedClient = new Client();
        savedClient.setId(1L);
        savedClient.setPhone("+79991234567");
        savedClient.setFirstName("Test");
        
        when(clientRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);
        when(bookingStatusRepository.findByCodeAndIsActiveTrue("PENDING"))
                .thenReturn(Optional.of(pendingStatus));
        when(preOrderRepository.save(any(PreOrder.class))).thenAnswer(invocation -> {
            PreOrder preOrder = invocation.getArgument(0);
            preOrder.setId(1L);
            // Убеждаемся, что client установлен и имеет ID
            if (preOrder.getClient() == null) {
                preOrder.setClient(savedClient);
            } else if (preOrder.getClient().getId() == null) {
                preOrder.getClient().setId(1L);
            }
            return preOrder;
        });
        
        // When
        CreatePreOrderResponse response = preOrderService.createPreOrder(request);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getRestaurantId());
        assertNotNull(response.getTotalAmount());
        verify(preOrderRepository).save(any(PreOrder.class));
        verify(preOrderItemRepository, atLeastOnce()).save(any(PreOrderItem.class));
        verify(preOrderHistoryRepository).save(any(PreOrderHistory.class));
    }
    
    @Test
    void testCreatePreOrderRestaurantNotFound() {
        // Given
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(RuntimeException.class, () -> preOrderService.createPreOrder(request));
    }
    
    @Test
    void testCreatePreOrderMenuItemNotFound() {
        // Given
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(RuntimeException.class, () -> preOrderService.createPreOrder(request));
    }
}

