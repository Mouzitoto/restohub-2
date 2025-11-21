package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    List<Room> findByFloorIdAndIsActiveTrue(Long floorId);
    Optional<Room> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT r FROM Room r JOIN r.floor f WHERE r.id = :roomId AND f.restaurant.id = :restaurantId AND r.isActive = true")
    Optional<Room> findByIdAndRestaurantIdAndIsActiveTrue(@Param("roomId") Long roomId, @Param("restaurantId") Long restaurantId);
}

