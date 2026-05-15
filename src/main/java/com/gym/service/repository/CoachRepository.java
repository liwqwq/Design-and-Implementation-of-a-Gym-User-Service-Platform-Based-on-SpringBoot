package com.gym.service.repository;

import com.gym.service.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {
    Optional<Coach> findByUsername(String username);
    Optional<Coach> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}