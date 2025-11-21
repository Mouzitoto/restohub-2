package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long>, JpaSpecificationExecutor<Floor> {
    List<Floor> findByRestaurantIdAndIsActiveTrue(Long restaurantId);
    Optional<Floor> findByIdAndIsActiveTrue(Long id);
    Optional<Floor> findByRestaurantIdAndFloorNumberAndIsActiveTrue(Long restaurantId, String floorNumber);
    Optional<Floor> findByIdAndRestaurantIdAndIsActiveTrue(Long id, Long restaurantId);
}

