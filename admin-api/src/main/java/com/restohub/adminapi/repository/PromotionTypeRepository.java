package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.PromotionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionTypeRepository extends JpaRepository<PromotionType, Long> {
    List<PromotionType> findByIsActiveTrue();
    Optional<PromotionType> findByIdAndIsActiveTrue(Long id);
    Optional<PromotionType> findByCodeAndIsActiveTrue(String code);
}

