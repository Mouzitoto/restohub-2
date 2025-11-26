package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.RestaurantSubscription;
import com.restohub.adminapi.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantSubscriptionRepository extends JpaRepository<RestaurantSubscription, Long>, JpaSpecificationExecutor<RestaurantSubscription> {
    List<RestaurantSubscription> findByRestaurantId(Long restaurantId);
    List<RestaurantSubscription> findByRestaurantIdAndIsActiveTrue(Long restaurantId);
    Optional<RestaurantSubscription> findByIdAndIsActiveTrue(Long id);
    Optional<RestaurantSubscription> findByPaymentReference(String paymentReference);
    List<RestaurantSubscription> findByStatusAndCreatedAtBefore(SubscriptionStatus status, LocalDateTime date);
    List<RestaurantSubscription> findByRestaurantIdAndStatus(Long restaurantId, SubscriptionStatus status);
    List<RestaurantSubscription> findByStatus(SubscriptionStatus status);
}

