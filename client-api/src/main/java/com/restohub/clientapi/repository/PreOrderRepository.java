package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.PreOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreOrderRepository extends JpaRepository<PreOrder, Long> {
    List<PreOrder> findByRestaurantId(Long restaurantId);
    List<PreOrder> findByClientId(Long clientId);
}

