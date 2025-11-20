package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.BookingPreOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingPreOrderRepository extends JpaRepository<BookingPreOrder, Long> {
    List<BookingPreOrder> findByBookingId(Long bookingId);
}

