package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByRestaurantId(Long restaurantId);
    List<Booking> findByClientId(Long clientId);
    Optional<Booking> findById(Long id);
}

