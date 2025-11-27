package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.PreOrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreOrderHistoryRepository extends JpaRepository<PreOrderHistory, Long> {
    List<PreOrderHistory> findByPreOrderId(Long preOrderId);
    List<PreOrderHistory> findByPreOrderIdOrderByChangedAtDesc(Long preOrderId);
}

