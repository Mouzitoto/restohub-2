package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingStatusRepository extends JpaRepository<BookingStatus, Long>, JpaSpecificationExecutor<BookingStatus> {
    List<BookingStatus> findByIsActiveTrueOrderByDisplayOrderAsc();
    Optional<BookingStatus> findByIdAndIsActiveTrue(Long id);
    Optional<BookingStatus> findByCodeAndIsActiveTrue(String code);
}

