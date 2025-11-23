package com.restohub.clientapi.service;

import com.restohub.clientapi.entity.Restaurant;
import com.restohub.clientapi.entity.RestaurantSubscription;
import com.restohub.clientapi.repository.RestaurantRepository;
import com.restohub.clientapi.repository.RestaurantSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionCheckService {
    
    private final RestaurantRepository restaurantRepository;
    private final RestaurantSubscriptionRepository subscriptionRepository;
    
    @Autowired
    public SubscriptionCheckService(
            RestaurantRepository restaurantRepository,
            RestaurantSubscriptionRepository subscriptionRepository) {
        this.restaurantRepository = restaurantRepository;
        this.subscriptionRepository = subscriptionRepository;
    }
    
    /**
     * Проверяет наличие активной подписки у ресторана.
     * Кэширует только положительные результаты (наличие подписки).
     * 
     * @param restaurantId ID ресторана
     * @return true если у ресторана есть активная подписка, false в противном случае
     */
    @Cacheable(value = "subscriptionCache", key = "#restaurantId", unless = "#result == false")
    public boolean hasActiveSubscription(Long restaurantId) {
        // Проверяем существование ресторана
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(restaurantId)
                .orElse(null);
        
        if (restaurant == null) {
            return false;
        }
        
        // Получаем все активные подписки ресторана
        List<RestaurantSubscription> subscriptions = subscriptionRepository
                .findByRestaurantIdAndIsActiveTrue(restaurantId);
        
        if (subscriptions.isEmpty()) {
            return false;
        }
        
        // Проверяем, есть ли подписка, которая активна на текущую дату
        LocalDate today = LocalDate.now();
        
        return subscriptions.stream()
                .anyMatch(subscription -> 
                    subscription.getIsActive() &&
                    !subscription.getStartDate().isAfter(today) &&
                    !subscription.getEndDate().isBefore(today)
                );
    }
}

