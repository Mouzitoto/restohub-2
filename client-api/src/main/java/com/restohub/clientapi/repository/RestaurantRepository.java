package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByIsActiveTrue();
    Optional<Restaurant> findByIdAndIsActiveTrue(Long id);
}

