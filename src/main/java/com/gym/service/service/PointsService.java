package com.gym.service.service;

import com.gym.service.entity.Points;
import com.gym.service.entity.PointsExchange;
import com.gym.service.entity.PointsProduct;

import java.util.List;

public interface PointsService {
    Points getPointsByUserId(Long userId);
    void addPoints(Long userId, Integer points, String reason);
    void deductPoints(Long userId, Integer points, String reason);
    PointsExchange exchangePoints(Long userId, Long productId);
    List<PointsProduct> getAllProducts();
    List<PointsProduct> getAllProductsForAdmin();
    PointsProduct createProductForAdmin(PointsProduct product);
    PointsProduct updateProductForAdmin(Long id, PointsProduct product);
    void deleteProductForAdmin(Long id);
    List<PointsProduct> getVipProducts();
    List<PointsProduct> getProductsByCategory(String category);
    List<PointsExchange> getUserExchangeHistory(Long userId);
}
