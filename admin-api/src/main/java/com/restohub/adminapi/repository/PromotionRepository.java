package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByRestaurantIdAndIsActiveTrue(Long restaurantId);
    Optional<Promotion> findByIdAndIsActiveTrue(Long id);
}

