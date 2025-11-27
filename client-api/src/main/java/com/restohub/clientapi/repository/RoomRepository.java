package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByFloorIdAndIsActiveTrue(Long floorId);
    Optional<Room> findByIdAndIsActiveTrue(Long id);
    
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Room r JOIN r.floor f " +
            "WHERE f.restaurant.id = :restaurantId AND r.isActive = true " +
            "AND (:floorId IS NULL OR f.id = :floorId) ORDER BY f.floorNumber, r.name")
    List<Room> findByRestaurantIdAndFloorIdOptional(@Param("restaurantId") Long restaurantId, @Param("floorId") Long floorId);
}

