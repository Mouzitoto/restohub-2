package com.restohub.adminapi.service;

import com.restohub.adminapi.entity.RestaurantSubscription;
import com.restohub.adminapi.entity.SubscriptionStatus;
import com.restohub.adminapi.repository.RestaurantSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionSchedulerTest {
    
    @Mock
    private RestaurantSubscriptionRepository subscriptionRepository;
    
    @InjectMocks
    private SubscriptionScheduler subscriptionScheduler;
    
    private RestaurantSubscription draftSubscription;
    private RestaurantSubscription activatedSubscription;
    
    @BeforeEach
    void setUp() {
        draftSubscription = new RestaurantSubscription();
        draftSubscription.setId(1L);
        draftSubscription.setStatus(SubscriptionStatus.DRAFT);
        draftSubscription.setCreatedAt(LocalDateTime.now().minusDays(8));
        draftSubscription.setIsActive(false);
        
        activatedSubscription = new RestaurantSubscription();
        activatedSubscription.setId(2L);
        activatedSubscription.setStatus(SubscriptionStatus.ACTIVATED);
        activatedSubscription.setEndDate(LocalDate.now().minusDays(1));
        activatedSubscription.setIsActive(true);
    }
    
    @Test
    void testCheckExpiredSubscriptions_ExpiresDraftSubscriptions() {
        // Arrange
        List<RestaurantSubscription> draftSubscriptions = Arrays.asList(draftSubscription);
        when(subscriptionRepository.findByStatusAndCreatedAtBefore(
                eq(SubscriptionStatus.DRAFT), any(LocalDateTime.class)))
                .thenReturn(draftSubscriptions);
        when(subscriptionRepository.save(any(RestaurantSubscription.class)))
                .thenReturn(draftSubscription);
        
        // Act
        subscriptionScheduler.checkExpiredSubscriptions();
        
        // Assert
        verify(subscriptionRepository, times(1)).save(draftSubscription);
        assertEquals(SubscriptionStatus.EXPIRED, draftSubscription.getStatus());
        assertFalse(draftSubscription.getIsActive());
    }
    
    @Test
    void testCheckExpiredSubscriptions_NoExpiredDraftSubscriptions() {
        // Arrange
        when(subscriptionRepository.findByStatusAndCreatedAtBefore(
                eq(SubscriptionStatus.DRAFT), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        
        // Act
        subscriptionScheduler.checkExpiredSubscriptions();
        
        // Assert
        verify(subscriptionRepository, never()).save(any(RestaurantSubscription.class));
    }
    
    @Test
    void testCheckActiveSubscriptionsExpiration_ExpiresActivatedSubscriptions() {
        // Arrange
        List<RestaurantSubscription> activatedSubscriptions = Arrays.asList(activatedSubscription);
        when(subscriptionRepository.findByStatusAndIsActiveTrue(SubscriptionStatus.ACTIVATED))
                .thenReturn(activatedSubscriptions);
        when(subscriptionRepository.save(any(RestaurantSubscription.class)))
                .thenReturn(activatedSubscription);
        
        // Act
        subscriptionScheduler.checkActiveSubscriptionsExpiration();
        
        // Assert
        verify(subscriptionRepository, times(1)).save(activatedSubscription);
        assertEquals(SubscriptionStatus.EXPIRED, activatedSubscription.getStatus());
        assertFalse(activatedSubscription.getIsActive());
    }
    
    @Test
    void testCheckActiveSubscriptionsExpiration_NoExpiredActivatedSubscriptions() {
        // Arrange
        activatedSubscription.setEndDate(LocalDate.now().plusDays(10));
        List<RestaurantSubscription> activatedSubscriptions = Arrays.asList(activatedSubscription);
        when(subscriptionRepository.findByStatusAndIsActiveTrue(SubscriptionStatus.ACTIVATED))
                .thenReturn(activatedSubscriptions);
        
        // Act
        subscriptionScheduler.checkActiveSubscriptionsExpiration();
        
        // Assert
        verify(subscriptionRepository, never()).save(any(RestaurantSubscription.class));
    }
}

