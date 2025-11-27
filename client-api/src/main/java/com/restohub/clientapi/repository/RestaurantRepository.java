package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByIsActiveTrue();
    Optional<Restaurant> findByIdAndIsActiveTrue(Long id);
    
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT r FROM Restaurant r " +
            "WHERE r.isActive = true " +
            "AND (:q IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Restaurant> searchRestaurants(@Param("q") String q);
}

