package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    List<MenuCategory> findByIsActiveTrueOrderByDisplayOrderAsc();
    Optional<MenuCategory> findByIdAndIsActiveTrue(Long id);
    Optional<MenuCategory> findByNameAndIsActiveTrue(String name);
}

