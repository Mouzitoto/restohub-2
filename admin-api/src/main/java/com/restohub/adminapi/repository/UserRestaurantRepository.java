package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.UserRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRestaurantRepository extends JpaRepository<UserRestaurant, Long> {
    List<UserRestaurant> findByUserId(Long userId);
    List<UserRestaurant> findByRestaurantId(Long restaurantId);
    
    @Query("SELECT ur FROM UserRestaurant ur JOIN ur.user u WHERE u.email = :email")
    List<UserRestaurant> findByUserEmail(@Param("email") String email);
}

