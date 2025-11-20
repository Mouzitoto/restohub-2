package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIsActiveTrue();
    Optional<User> findByIdAndIsActiveTrue(Long id);
    Optional<User> findByEmailAndIsActiveTrue(String email);
}

