package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByRoomIdAndIsActiveTrue(Long roomId);
    Optional<RestaurantTable> findByIdAndIsActiveTrue(Long id);
    
    @org.springframework.data.jpa.repository.Query("SELECT t FROM RestaurantTable t JOIN t.room r JOIN r.floor f " +
            "WHERE f.restaurant.id = :restaurantId AND t.isActive = true " +
            "AND (:roomId IS NULL OR r.id = :roomId) " +
            "AND (:floorId IS NULL OR f.id = :floorId) " +
            "ORDER BY r.name, t.tableNumber")
    List<RestaurantTable> findByRestaurantIdAndRoomIdAndFloorIdOptional(@Param("restaurantId") Long restaurantId, @Param("roomId") Long roomId, @Param("floorId") Long floorId);
}

