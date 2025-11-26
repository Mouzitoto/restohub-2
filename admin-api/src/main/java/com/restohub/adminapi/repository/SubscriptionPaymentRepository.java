package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {
    Optional<SubscriptionPayment> findByExternalTransactionId(String externalTransactionId);
    List<SubscriptionPayment> findBySubscriptionId(Long subscriptionId);
}

