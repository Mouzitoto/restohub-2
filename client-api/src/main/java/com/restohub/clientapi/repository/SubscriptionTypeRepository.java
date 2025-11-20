package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionTypeRepository extends JpaRepository<SubscriptionType, Long> {
    List<SubscriptionType> findByIsActiveTrue();
    Optional<SubscriptionType> findByIdAndIsActiveTrue(Long id);
    Optional<SubscriptionType> findByCodeAndIsActiveTrue(String code);
}

