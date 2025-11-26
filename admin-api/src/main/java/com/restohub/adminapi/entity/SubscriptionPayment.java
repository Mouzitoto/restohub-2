package com.restohub.adminapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_payments")
@Getter
@Setter
public class SubscriptionPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private RestaurantSubscription subscription;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
    
    @Column(name = "external_transaction_id", unique = true, length = 100)
    private String externalTransactionId;
    
    @Column(name = "payment_reference", length = 50)
    private String paymentReference;
    
    @Column(name = "status", nullable = false, length = 20)
    @Convert(converter = PaymentStatus.PaymentStatusConverter.class)
    private PaymentStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

