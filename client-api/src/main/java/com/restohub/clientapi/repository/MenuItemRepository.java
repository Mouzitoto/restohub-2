package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndIsActiveTrue(Long restaurantId);
    List<MenuItem> findByRestaurantIdAndMenuCategoryIdAndIsActiveTrue(Long restaurantId, Long menuCategoryId);
    Optional<MenuItem> findByIdAndIsActiveTrue(Long id);
    
    @org.springframework.data.jpa.repository.Query("SELECT mi FROM MenuItem mi WHERE mi.restaurant.id = :restaurantId " +
            "AND mi.isActive = true AND mi.isAvailable = true ORDER BY mi.menuCategory.displayOrder, mi.displayOrder")
    List<MenuItem> findByRestaurantIdAndIsActiveTrueAndIsAvailableTrue(@Param("restaurantId") Long restaurantId);
    
    @org.springframework.data.jpa.repository.Query("SELECT mi FROM MenuItem mi WHERE mi.restaurant.id = :restaurantId " +
            "AND mi.menuCategory.id = :categoryId AND mi.isActive = true AND mi.isAvailable = true " +
            "ORDER BY mi.displayOrder")
    List<MenuItem> findByRestaurantIdAndCategoryIdAndIsActiveTrueAndIsAvailableTrue(@Param("restaurantId") Long restaurantId, @Param("categoryId") Long categoryId);
}

