package com.restohub.clientapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Getter
@Setter
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "phone", unique = true, nullable = false, length = 50)
    private String phone;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "first_booking_date")
    private LocalDateTime firstBookingDate;
    
    @Column(name = "last_booking_date")
    private LocalDateTime lastBookingDate;
    
    @Column(name = "total_bookings", nullable = false)
    private Integer totalBookings = 0;
    
    @Column(name = "total_pre_orders", nullable = false)
    private Integer totalPreOrders = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

