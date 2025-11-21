package com.restohub.adminapi.repository;

import com.restohub.adminapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    List<User> findByIsActiveTrue();
    Optional<User> findByIdAndIsActiveTrue(Long id);
    Optional<User> findByEmailAndIsActiveTrue(String email);
    Optional<User> findByEmail(String email);
}

