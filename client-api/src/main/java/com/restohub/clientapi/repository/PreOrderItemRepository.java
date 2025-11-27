package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.PreOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreOrderItemRepository extends JpaRepository<PreOrderItem, Long> {
    List<PreOrderItem> findByPreOrderId(Long preOrderId);
}

