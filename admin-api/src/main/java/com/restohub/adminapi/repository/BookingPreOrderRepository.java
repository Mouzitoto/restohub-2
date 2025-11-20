package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.BookingPreOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingPreOrderRepository extends JpaRepository<BookingPreOrder, Long> {
    List<BookingPreOrder> findByBookingId(Long bookingId);
}

