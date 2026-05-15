package com.gym.service.repository;

import com.gym.service.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByUserId(Long userId);
    List<Badge> findByUserIdAndUnlocked(Long userId, Boolean unlocked);
}
