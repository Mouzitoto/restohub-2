package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByRoomIdAndIsActiveTrue(Long roomId);
    Optional<RestaurantTable> findByIdAndIsActiveTrue(Long id);
}

