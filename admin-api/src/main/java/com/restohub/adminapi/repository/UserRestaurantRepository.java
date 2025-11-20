package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.UserRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRestaurantRepository extends JpaRepository<UserRestaurant, Long> {
    List<UserRestaurant> findByUserId(Long userId);
    List<UserRestaurant> findByRestaurantId(Long restaurantId);
}

