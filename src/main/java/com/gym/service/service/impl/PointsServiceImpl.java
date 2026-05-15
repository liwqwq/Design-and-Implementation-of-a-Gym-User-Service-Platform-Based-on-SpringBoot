package com.gym.service.service.impl;

import com.gym.service.entity.Points;
import com.gym.service.entity.PointsExchange;
import com.gym.service.entity.PointsProduct;
import com.gym.service.repository.PointsExchangeRepository;
import com.gym.service.repository.PointsProductRepository;
import com.gym.service.repository.PointsRepository;
import com.gym.service.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PointsServiceImpl implements PointsService {

    @Autowired
    private PointsRepository pointsRepository;

    @Autowired
    private PointsProductRepository pointsProductRepository;

    @Autowired
    private PointsExchangeRepository pointsExchangeRepository;

    @Override
    public Points getPointsByUserId(Long userId) {
        return pointsRepository.findByUserId(userId).orElseGet(() -> {
            Points points = new Points();
            points.setUserId(userId);
            points.setTotalPoints(0);
            points.setAvailablePoints(0);
            points.setUsedPoints(0);
            points.setConsecutiveDays(0);
            points.setCreatedAt(LocalDateTime.now());
            points.setUpdatedAt(LocalDateTime.now());
            return pointsRepository.save(points);
        });
    }

    @Override
    @Transactional
    public void addPoints(Long userId, Integer points, String reason) {
        Points userPoints = getPointsByUserId(userId);
        userPoints.setTotalPoints(userPoints.getTotalPoints() + points);
        userPoints.setAvailablePoints(userPoints.getAvailablePoints() + points);
        userPoints.setUpdatedAt(LocalDateTime.now());
        pointsRepository.save(userPoints);
    }

    @Override
    @Transactional
    public void deductPoints(Long userId, Integer points, String reason) {
        Points userPoints = getPointsByUserId(userId);
        if (userPoints.getAvailablePoints() < points) {
            throw new RuntimeException("积分不足");
        }
        userPoints.setAvailablePoints(userPoints.getAvailablePoints() - points);
        userPoints.setUsedPoints(userPoints.getUsedPoints() + points);
        userPoints.setUpdatedAt(LocalDateTime.now());
        pointsRepository.save(userPoints);
    }

    @Override
    @Transactional
    public PointsExchange exchangePoints(Long userId, Long productId) {
        PointsProduct product = pointsProductRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        if (!product.getIsActive()) {
            throw new RuntimeException("商品已下架");
        }
        
        if (product.getStockQuantity() != null && product.getStockQuantity() <= 0) {
            throw new RuntimeException("商品库存不足");
        }

        Points userPoints = getPointsByUserId(userId);
        if (userPoints.getAvailablePoints() < product.getPointsCost()) {
            throw new RuntimeException("积分不足");
        }

        deductPoints(userId, product.getPointsCost(), "兑换商品：" + product.getName());

        if (product.getStockQuantity() != null) {
            product.setStockQuantity(product.getStockQuantity() - 1);
        }
        if (product.getSoldCount() == null) {
            product.setSoldCount(0);
        }
        product.setSoldCount(product.getSoldCount() + 1);
        product.setUpdatedAt(LocalDateTime.now());
        pointsProductRepository.save(product);

        PointsExchange exchange = new PointsExchange();
        exchange.setUserId(userId);
        exchange.setProductId(productId);
        exchange.setProductName(product.getName());
        exchange.setProductNameEn(product.getNameEn());
        exchange.setPointsUsed(product.getPointsCost());
        exchange.setStatus("PENDING");
        exchange.setCreatedAt(LocalDateTime.now());
        return pointsExchangeRepository.save(exchange);
    }

    @Override
    public List<PointsProduct> getAllProducts() {
        return pointsProductRepository.findByIsActiveTrueOrderBySoldCountDesc();
    }


    @Override
    public List<PointsProduct> getAllProductsForAdmin() {
        return pointsProductRepository.findAllByOrderByIdAsc();
    }

    @Override
    @Transactional
    public PointsProduct createProductForAdmin(PointsProduct product) {
        applyAdminProductDefaults(product);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return pointsProductRepository.save(product);
    }

    @Override
    @Transactional
    public PointsProduct updateProductForAdmin(Long id, PointsProduct productDetails) {
        PointsProduct product = pointsProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("积分商品不存在"));
        product.setName(productDetails.getName());
        product.setNameEn(productDetails.getNameEn());
        product.setDescription(productDetails.getDescription());
        product.setDescriptionEn(productDetails.getDescriptionEn());
        product.setPointsCost(productDetails.getPointsCost());
        product.setCategory(productDetails.getCategory());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setIcon(productDetails.getIcon());
        product.setIsActive(productDetails.getIsActive());
        product.setIsVip(productDetails.getIsVip());
        product.setDiscountType(productDetails.getDiscountType());
        product.setMinSpend(productDetails.getMinSpend());
        product.setDiscountAmount(productDetails.getDiscountAmount());
        product.setExpireDays(productDetails.getExpireDays());
        applyAdminProductDefaults(product);
        product.setUpdatedAt(LocalDateTime.now());
        return pointsProductRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProductForAdmin(Long id) {
        PointsProduct product = pointsProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("积分商品不存在"));
        if (pointsExchangeRepository.existsByProductId(id)) {
            product.setIsActive(false);
            product.setUpdatedAt(LocalDateTime.now());
            pointsProductRepository.save(product);
        } else {
            pointsProductRepository.deleteById(id);
        }
    }

    private void applyAdminProductDefaults(PointsProduct product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new RuntimeException("商品名称不能为空");
        }
        product.setName(product.getName().trim());
        if (product.getNameEn() != null) {
            product.setNameEn(product.getNameEn().trim());
        }
        if (product.getDescription() == null) {
            product.setDescription("");
        }
        if (product.getDescriptionEn() != null) {
            product.setDescriptionEn(product.getDescriptionEn().trim());
        }
        if (product.getPointsCost() == null || product.getPointsCost() < 0) {
            product.setPointsCost(0);
        }
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            product.setCategory("运动装备");
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
        if (product.getSoldCount() == null) {
            product.setSoldCount(0);
        }
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }
        if (product.getIsVip() == null) {
            product.setIsVip(isVipProduct(product));
        }
        if (product.getIcon() == null || product.getIcon().trim().isEmpty()) {
            product.setIcon(defaultIcon(product));
        }
        if (product.getDiscountType() == null || product.getDiscountType().trim().isEmpty()) {
            product.setDiscountType(defaultDiscountType(product));
        }
    }

    private String defaultDiscountType(PointsProduct product) {
        String category = normalizeCategory(product.getCategory());
        if (category.equals("优惠券") || category.equals("coupon")) {
            return "COUPON";
        }
        if (category.equals("会员服务") || category.equals("service")) {
            return "SERVICE";
        }
        if (category.equals("会员特权") || category.equals("privilege")) {
            return "PRIVILEGE";
        }
        return "PRODUCT";
    }

    private String defaultIcon(PointsProduct product) {
        String category = normalizeCategory(product.getCategory());
        String name = normalizeCategory(product.getName() + " " + product.getNameEn());
        if (category.equals("优惠券") || name.contains("券") || name.contains("coupon")) return "🎟️";
        if (category.contains("vip") || name.contains("vip")) return "👑";
        if (name.contains("瑜伽") || name.contains("yoga")) return "🧘";
        if (name.contains("蛋白") || name.contains("protein")) return "💪";
        if (name.contains("衣") || name.contains("shirt") || name.contains("tee")) return "👕";
        if (name.contains("手环") || name.contains("watch") || name.contains("band")) return "⌚";
        return "🎁";
    }

    @Override
    public List<PointsProduct> getVipProducts() {
        return pointsProductRepository.findByIsVipTrueAndIsActiveTrueOrderBySoldCountDesc();
    }

    @Override
    public List<PointsProduct> getProductsByCategory(String category) {
        String requested = normalizeCategory(category);
        if (requested.equals("coupon")) {
            return getAllProducts().stream().filter(this::isCouponProduct).collect(Collectors.toList());
        }
        if (requested.equals("product") || requested.equals("equipment")) {
            return getAllProducts().stream().filter(this::isEquipmentProduct).collect(Collectors.toList());
        }
        if (requested.equals("vip")) {
            return getAllProducts().stream().filter(this::isVipProduct).collect(Collectors.toList());
        }
        return pointsProductRepository.findByCategoryAndIsActiveTrueOrderBySoldCountDesc(category);
    }

    private String normalizeCategory(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isCouponProduct(PointsProduct product) {
        String discountType = normalizeCategory(product.getDiscountType());
        String category = normalizeCategory(product.getCategory());
        return discountType.equals("coupon") || category.equals("优惠券") || category.equals("coupon") || category.equals("coupons");
    }

    private boolean isVipProduct(PointsProduct product) {
        String category = normalizeCategory(product.getCategory());
        return Boolean.TRUE.equals(product.getIsVip()) || category.contains("vip") || category.equals("vip专属");
    }

    private boolean isEquipmentProduct(PointsProduct product) {
        String discountType = normalizeCategory(product.getDiscountType());
        String category = normalizeCategory(product.getCategory());
        return !isVipProduct(product) && (discountType.equals("product")
                || category.equals("运动装备")
                || category.equals("训练辅助")
                || category.equals("营养补剂")
                || category.equals("健身服饰")
                || category.equals("运动配件")
                || category.equals("equipment")
                || category.equals("training accessories")
                || category.equals("supplements")
                || category.equals("apparel")
                || category.equals("sports accessories"));
    }

    @Override
    public List<PointsExchange> getUserExchangeHistory(Long userId) {
        return pointsExchangeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
