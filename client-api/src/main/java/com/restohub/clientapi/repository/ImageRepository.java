package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByIsActiveTrue();
    Optional<Image> findByIdAndIsActiveTrue(Long id);
}

