package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.RestaurantSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantSubscriptionRepository extends JpaRepository<RestaurantSubscription, Long>, JpaSpecificationExecutor<RestaurantSubscription> {
    List<RestaurantSubscription> findByRestaurantId(Long restaurantId);
    List<RestaurantSubscription> findByRestaurantIdAndIsActiveTrue(Long restaurantId);
    Optional<RestaurantSubscription> findByIdAndIsActiveTrue(Long id);
}

