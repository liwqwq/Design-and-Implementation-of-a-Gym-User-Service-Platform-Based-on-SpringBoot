package com.gym.service.repository;

import com.gym.service.entity.PointsProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointsProductRepository extends JpaRepository<PointsProduct, Long> {
    List<PointsProduct> findAllByOrderByIdAsc();
    Optional<PointsProduct> findFirstByName(String name);
    List<PointsProduct> findByIsActiveTrueOrderBySoldCountDesc();
    List<PointsProduct> findByCategoryAndIsActiveTrueOrderBySoldCountDesc(String category);
    List<PointsProduct> findByIsVipTrueAndIsActiveTrueOrderBySoldCountDesc();
}
