package com.restohub.adminapi.service;

import com.restohub.adminapi.dto.*;
import com.restohub.adminapi.entity.*;
import com.restohub.adminapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
    
    @Mock
    private RestaurantSubscriptionRepository subscriptionRepository;
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private SubscriptionTypeRepository subscriptionTypeRepository;
    
    @Mock
    private SubscriptionPaymentRepository paymentRepository;
    
    @InjectMocks
    private SubscriptionService subscriptionService;
    
    private Restaurant restaurant;
    private SubscriptionType subscriptionType;
    private RestaurantSubscription subscription;
    
    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Restaurant");
        restaurant.setIsActive(true);
        
        subscriptionType = new SubscriptionType();
        subscriptionType.setId(1L);
        subscriptionType.setCode("STANDARD");
        subscriptionType.setName("Стандарт");
        subscriptionType.setPrice(new BigDecimal("10000.00"));
        subscriptionType.setIsActive(true);
        
        subscription = new RestaurantSubscription();
        subscription.setId(1L);
        subscription.setRestaurant(restaurant);
        subscription.setSubscriptionType(subscriptionType);
        subscription.setStatus(SubscriptionStatus.DRAFT);
        subscription.setPaymentReference("SUB-2024-123456");
        subscription.setIsActive(false);
    }
    
    @Test
    void testCreateSubscription_Success() {
        // Arrange
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setSubscriptionTypeId(1L);
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(subscriptionTypeRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(subscriptionType));
        when(subscriptionRepository.findByRestaurantId(1L))
                .thenReturn(Arrays.asList());
        when(subscriptionRepository.save(any(RestaurantSubscription.class)))
                .thenAnswer(invocation -> {
                    RestaurantSubscription sub = invocation.getArgument(0);
                    sub.setId(1L);
                    return sub;
                });
        
        // Act
        SubscriptionResponse response = subscriptionService.createSubscription(1L, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getRestaurantId());
        assertNotNull(response.getPaymentReference());
        assertTrue(response.getPaymentReference().startsWith("SUB-1-"));
        assertEquals("SUB-1-1", response.getPaymentReference());
        verify(subscriptionRepository, times(1)).save(any(RestaurantSubscription.class));
    }
    
    @Test
    void testCreateSubscription_RestaurantNotFound() {
        // Arrange
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setSubscriptionTypeId(1L);
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            subscriptionService.createSubscription(1L, request);
        });
    }
    
    @Test
    void testActivateSubscription_Success() {
        // Arrange
        ActivateSubscriptionRequest request = new ActivateSubscriptionRequest();
        request.setPaymentReference("SUB-2024-123456");
        request.setAmount(new BigDecimal("10000.00"));
        request.setPaymentDate(LocalDateTime.now());
        request.setExternalTransactionId("TXN-123");
        
        subscription.setStatus(SubscriptionStatus.DRAFT);
        
        when(subscriptionRepository.findByPaymentReference("SUB-2024-123456"))
                .thenReturn(Optional.of(subscription));
        when(paymentRepository.findByExternalTransactionId("TXN-123"))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(RestaurantSubscription.class)))
                .thenReturn(subscription);
        when(paymentRepository.save(any(SubscriptionPayment.class)))
                .thenReturn(new SubscriptionPayment());
        
        // Act
        SubscriptionResponse response = subscriptionService.activateSubscription(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(SubscriptionStatus.ACTIVATED, subscription.getStatus());
        assertTrue(subscription.getIsActive());
        assertNotNull(subscription.getStartDate());
        assertNotNull(subscription.getEndDate());
        verify(paymentRepository, times(1)).save(any(SubscriptionPayment.class));
    }
    
    @Test
    void testActivateSubscription_Idempotency() {
        // Arrange
        ActivateSubscriptionRequest request = new ActivateSubscriptionRequest();
        request.setPaymentReference("SUB-2024-123456");
        request.setAmount(new BigDecimal("10000.00"));
        request.setPaymentDate(LocalDateTime.now());
        request.setExternalTransactionId("TXN-123");
        
        subscription.setStatus(SubscriptionStatus.ACTIVATED);
        
        SubscriptionPayment existingPayment = new SubscriptionPayment();
        existingPayment.setExternalTransactionId("TXN-123");
        
        when(subscriptionRepository.findByPaymentReference("SUB-2024-123456"))
                .thenReturn(Optional.of(subscription));
        when(paymentRepository.findByExternalTransactionId("TXN-123"))
                .thenReturn(Optional.of(existingPayment));
        
        // Act
        SubscriptionResponse response = subscriptionService.activateSubscription(request);
        
        // Assert
        assertNotNull(response);
        verify(paymentRepository, never()).save(any(SubscriptionPayment.class));
    }
    
    @Test
    void testActivateSubscription_InvalidStatus() {
        // Arrange
        ActivateSubscriptionRequest request = new ActivateSubscriptionRequest();
        request.setPaymentReference("SUB-2024-123456");
        request.setAmount(new BigDecimal("10000.00"));
        request.setPaymentDate(LocalDateTime.now());
        request.setExternalTransactionId("TXN-123");
        
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        
        when(subscriptionRepository.findByPaymentReference("SUB-2024-123456"))
                .thenReturn(Optional.of(subscription));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            subscriptionService.activateSubscription(request);
        });
    }
    
    @Test
    void testGeneratePaymentReference_Unique() {
        // Arrange
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(subscriptionTypeRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(subscriptionType));
        when(subscriptionRepository.findByRestaurantId(1L))
                .thenReturn(Arrays.asList());
        when(subscriptionRepository.save(any(RestaurantSubscription.class)))
                .thenAnswer(invocation -> {
                    RestaurantSubscription sub = invocation.getArgument(0);
                    sub.setId(1L);
                    return sub;
                });
        
        // Act
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setSubscriptionTypeId(1L);
        SubscriptionResponse response = subscriptionService.createSubscription(1L, request);
        
        // Assert
        assertNotNull(response.getPaymentReference());
        assertTrue(response.getPaymentReference().startsWith("SUB-1-"));
        assertEquals("SUB-1-1", response.getPaymentReference());
    }
    
    @Test
    void testGeneratePaymentReference_Sequential() {
        // Arrange
        RestaurantSubscription existingSubscription1 = new RestaurantSubscription();
        existingSubscription1.setId(1L);
        existingSubscription1.setPaymentReference("SUB-1-1");
        
        RestaurantSubscription existingSubscription2 = new RestaurantSubscription();
        existingSubscription2.setId(2L);
        existingSubscription2.setPaymentReference("SUB-1-2");
        
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(subscriptionTypeRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(subscriptionType));
        when(subscriptionRepository.findByRestaurantId(1L))
                .thenReturn(Arrays.asList(existingSubscription1, existingSubscription2));
        when(subscriptionRepository.save(any(RestaurantSubscription.class)))
                .thenAnswer(invocation -> {
                    RestaurantSubscription sub = invocation.getArgument(0);
                    sub.setId(3L);
                    return sub;
                });
        
        // Act
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setSubscriptionTypeId(1L);
        SubscriptionResponse response = subscriptionService.createSubscription(1L, request);
        
        // Assert
        assertNotNull(response.getPaymentReference());
        // Должно быть 3, так как у нас уже есть 2 подписки с форматом SUB-1-*
        assertEquals("SUB-1-3", response.getPaymentReference());
    }

    @Test
    void testGetRestaurantSubscriptions_Success() {
        // Arrange
        RestaurantSubscription subscription1 = new RestaurantSubscription();
        subscription1.setId(1L);
        subscription1.setRestaurant(restaurant);
        subscription1.setSubscriptionType(subscriptionType);
        subscription1.setStatus(SubscriptionStatus.ACTIVATED);
        subscription1.setPaymentReference("SUB-2024-123456");
        subscription1.setStartDate(LocalDate.now());
        subscription1.setEndDate(LocalDate.now().plusMonths(1));
        subscription1.setIsActive(true);
        subscription1.setCreatedAt(LocalDateTime.now().minusDays(5));

        RestaurantSubscription subscription2 = new RestaurantSubscription();
        subscription2.setId(2L);
        subscription2.setRestaurant(restaurant);
        subscription2.setSubscriptionType(subscriptionType);
        subscription2.setStatus(SubscriptionStatus.DRAFT);
        subscription2.setPaymentReference("SUB-2024-123457");
        subscription2.setIsActive(false);
        subscription2.setCreatedAt(LocalDateTime.now().minusDays(2));

        List<RestaurantSubscription> subscriptions = Arrays.asList(subscription1, subscription2);

        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(subscriptionRepository.findByRestaurantId(1L))
                .thenReturn(subscriptions);

        // Act
        List<SubscriptionListItemResponse> response = subscriptionService.getRestaurantSubscriptions(1L);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        // Сортировка по дате создания (новые первые), поэтому subscription2 должен быть первым
        assertEquals(2L, response.get(0).getId());
        assertEquals("DRAFT", response.get(0).getStatus());
        assertEquals("SUB-2024-123457", response.get(0).getPaymentReference());
        assertEquals(false, response.get(0).getIsActive());
        assertEquals(1L, response.get(1).getId());
        assertEquals("ACTIVATED", response.get(1).getStatus());
        assertEquals("SUB-2024-123456", response.get(1).getPaymentReference());
        assertEquals(true, response.get(1).getIsActive());
        verify(subscriptionRepository, times(1)).findByRestaurantId(1L);
    }

    @Test
    void testGetRestaurantSubscriptions_RestaurantNotFound() {
        // Arrange
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            subscriptionService.getRestaurantSubscriptions(1L);
        });
    }

    @Test
    void testGetRestaurantSubscriptions_EmptyList() {
        // Arrange
        when(restaurantRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(restaurant));
        when(subscriptionRepository.findByRestaurantId(1L))
                .thenReturn(Arrays.asList());

        // Act
        List<SubscriptionListItemResponse> response = subscriptionService.getRestaurantSubscriptions(1L);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }
}

