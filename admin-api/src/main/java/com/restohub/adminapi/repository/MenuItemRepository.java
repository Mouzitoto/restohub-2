package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndIsActiveTrue(Long restaurantId);
    List<MenuItem> findByRestaurantIdAndMenuCategoryIdAndIsActiveTrue(Long restaurantId, Long menuCategoryId);
    Optional<MenuItem> findByIdAndIsActiveTrue(Long id);
}

