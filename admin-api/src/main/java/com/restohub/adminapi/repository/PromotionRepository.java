package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {
    List<Promotion> findByRestaurantIdAndIsActiveTrue(Long restaurantId);
    Optional<Promotion> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT p FROM Promotion p WHERE p.id = :promotionId AND p.restaurant.id = :restaurantId AND p.isActive = true")
    Optional<Promotion> findByIdAndRestaurantIdAndIsActiveTrue(@Param("promotionId") Long promotionId, @Param("restaurantId") Long restaurantId);
}
