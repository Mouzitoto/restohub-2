package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {
    List<Floor> findByRestaurantIdAndIsActiveTrue(Long restaurantId);
    Optional<Floor> findByIdAndIsActiveTrue(Long id);
}

