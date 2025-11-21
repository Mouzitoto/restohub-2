package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    List<Booking> findByRestaurantId(Long restaurantId);
    List<Booking> findByClientId(Long clientId);
    Optional<Booking> findById(Long id);
    
    @Query("SELECT b FROM Booking b JOIN b.table t JOIN t.room r JOIN r.floor f WHERE b.id = :bookingId AND f.restaurant.id = :restaurantId")
    Optional<Booking> findByIdAndRestaurantId(@Param("bookingId") Long bookingId, @Param("restaurantId") Long restaurantId);
}
