package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.PasswordResetCode;
import com.restohub.adminapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    
    @Query("SELECT prc FROM PasswordResetCode prc WHERE prc.user = :user AND prc.used = false AND prc.expiresAt > :now ORDER BY prc.createdAt DESC")
    Optional<PasswordResetCode> findActiveByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    Optional<PasswordResetCode> findByCode(String code);
    
    List<PasswordResetCode> findByUserAndUsedFalse(User user);
    
    void deleteByExpiresAtBefore(LocalDateTime now);
}

