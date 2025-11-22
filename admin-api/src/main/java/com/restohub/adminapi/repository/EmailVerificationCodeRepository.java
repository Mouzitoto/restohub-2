package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    
    @Query("SELECT e FROM EmailVerificationCode e WHERE e.userEmail = :email AND e.used = false AND e.expiresAt > :now ORDER BY e.createdAt DESC")
    List<EmailVerificationCode> findActiveCodesByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
    
    Optional<EmailVerificationCode> findFirstByUserEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String userEmail, LocalDateTime now);
    
    List<EmailVerificationCode> findByUserEmailAndUsedFalse(String userEmail);
}

