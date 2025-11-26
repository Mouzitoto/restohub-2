package com.restohub.adminapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant_subscriptions")
@Getter
@Setter
public class RestaurantSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_type_id", nullable = false)
    private SubscriptionType subscriptionType;
    
    @Column(name = "status", nullable = false, length = 20)
    @Convert(converter = SubscriptionStatus.SubscriptionStatusConverter.class)
    private SubscriptionStatus status = SubscriptionStatus.DRAFT;
    
    @Column(name = "payment_reference", unique = true, length = 50)
    private String paymentReference;
    
    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

