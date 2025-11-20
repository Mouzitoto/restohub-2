package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByFloorIdAndIsActiveTrue(Long floorId);
    Optional<Room> findByIdAndIsActiveTrue(Long id);
}

