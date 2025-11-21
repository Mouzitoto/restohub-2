package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Long>, JpaSpecificationExecutor<RestaurantTable> {
    List<RestaurantTable> findByRoomIdAndIsActiveTrue(Long roomId);
    Optional<RestaurantTable> findByIdAndIsActiveTrue(Long id);
    
    @org.springframework.data.jpa.repository.Query(
        "SELECT t FROM RestaurantTable t JOIN t.room r JOIN r.floor f WHERE t.id = :tableId AND f.restaurant.id = :restaurantId AND t.isActive = true"
    )
    Optional<RestaurantTable> findByIdAndRestaurantIdAndIsActiveTrue(
        @org.springframework.data.repository.query.Param("tableId") Long tableId,
        @org.springframework.data.repository.query.Param("restaurantId") Long restaurantId
    );
    
    Optional<RestaurantTable> findByRoomIdAndTableNumberAndIsActiveTrue(Long roomId, String tableNumber);
}

