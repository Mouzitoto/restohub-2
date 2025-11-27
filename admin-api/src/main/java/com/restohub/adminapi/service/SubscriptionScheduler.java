package com.restohub.adminapi.service;

import com.restohub.adminapi.entity.RestaurantSubscription;
import com.restohub.adminapi.entity.SubscriptionStatus;
import com.restohub.adminapi.repository.RestaurantSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "spring.task.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SubscriptionScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionScheduler.class);
    
    private final RestaurantSubscriptionRepository subscriptionRepository;
    
    @Autowired
    public SubscriptionScheduler(RestaurantSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }
    
    @Scheduled(cron = "0 0 0 * * ?") // Каждый день в 00:00
    @Transactional
    public void checkExpiredSubscriptions() {
        logger.info("Starting scheduled check for expired DRAFT subscriptions");
        
        // Находим подписки со статусом DRAFT старше 7 дней
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<RestaurantSubscription> expiredDraftSubscriptions = subscriptionRepository
                .findByStatusAndCreatedAtBefore(SubscriptionStatus.DRAFT, sevenDaysAgo);
        
        int count = 0;
        for (RestaurantSubscription subscription : expiredDraftSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setIsActive(false);
            subscriptionRepository.save(subscription);
            count++;
        }
        
        logger.info("Expired {} DRAFT subscriptions", count);
    }
    
    @Scheduled(cron = "0 0 0 * * ?") // Каждый день в 00:00
    @Transactional
    public void checkActiveSubscriptionsExpiration() {
        logger.info("Starting scheduled check for expired ACTIVATED subscriptions");
        
        // Находим все активные подписки со статусом ACTIVATED
        List<RestaurantSubscription> activatedSubscriptions = subscriptionRepository
                .findByStatusAndIsActiveTrue(SubscriptionStatus.ACTIVATED);
        
        int count = 0;
        java.time.LocalDate today = java.time.LocalDate.now();
        
        for (RestaurantSubscription subscription : activatedSubscriptions) {
            if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(today)) {
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                subscription.setIsActive(false);
                subscriptionRepository.save(subscription);
                count++;
            }
        }
        
        logger.info("Expired {} ACTIVATED subscriptions", count);
    }
}

