package com.restohub.clientapi.repository;

import com.restohub.clientapi.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByIsActiveTrue();
    Optional<Role> findByIdAndIsActiveTrue(Long id);
    Optional<Role> findByCodeAndIsActiveTrue(String code);
}

