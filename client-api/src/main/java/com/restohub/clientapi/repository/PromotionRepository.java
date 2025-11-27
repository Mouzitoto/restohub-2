package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByRestaurantIdAndIsActiveTrue(Long restaurantId);
    Optional<Promotion> findByIdAndIsActiveTrue(Long id);
    
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Promotion p WHERE p.restaurant.id = :restaurantId " +
            "AND p.isActive = true " +
            "AND (:promotionTypeId IS NULL OR p.promotionType.id = :promotionTypeId) " +
            "AND (:isCurrent IS NULL OR " +
            "(:isCurrent = true AND p.startDate <= CURRENT_DATE AND (p.endDate IS NULL OR p.endDate >= CURRENT_DATE)) OR " +
            "(:isCurrent = false)) " +
            "ORDER BY p.startDate DESC")
    List<Promotion> findActivePromotionsByRestaurant(@Param("restaurantId") Long restaurantId, 
                                                      @Param("promotionTypeId") Long promotionTypeId, 
                                                      @Param("isCurrent") Boolean isCurrent,
                                                      org.springframework.data.domain.Pageable pageable);
}

