package com.gym.service.repository;

import com.gym.service.entity.PointsExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsExchangeRepository extends JpaRepository<PointsExchange, Long> {
    List<PointsExchange> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PointsExchange> findByStatus(String status);
    boolean existsByProductId(Long productId);
}
