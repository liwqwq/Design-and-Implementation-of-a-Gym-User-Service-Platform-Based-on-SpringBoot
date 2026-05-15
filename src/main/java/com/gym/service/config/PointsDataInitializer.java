package com.gym.service.config;

import com.gym.service.entity.PointsProduct;
import com.gym.service.repository.PointsProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

// @Component - 已被 CompleteDataInitializer 替代
public class PointsDataInitializer implements CommandLineRunner {

    @Autowired
    private PointsProductRepository pointsProductRepository;

    @Override
    public void run(String... args) throws Exception {
        if (pointsProductRepository.count() > 0) {
            return;
        }

        List<PointsProduct> products = Arrays.asList(
            createProduct("5元无门槛券", "满0元可用，直减5元", 10, 0, 5, "COUPON", "🎟️", "优惠券", 999, 0, false, false, 30),
            createProduct("10元实付券", "实付满100元可用", 30, 100, 10, "COUPON", "🎫", "优惠券", 888, 0, false, false, 30),
            createProduct("15元满减券", "实付满200元可用", 50, 200, 15, "COUPON", "🎀", "优惠券", 777, 0, false, false, 30),
            createProduct("30元满减券", "实付满300元可用", 80, 300, 30, "COUPON", "🎁", "优惠券", 555, 0, false, false, 30),
            createProduct("运动水壶", "大容量运动水壶，便携耐用", 200, null, null, "PRODUCT", "🏋️", "运动装备", 100, 0, false, false, null),
            createProduct("瑜伽垫", "加厚防滑瑜伽垫", 300, null, null, "PRODUCT", "🧘", "运动装备", 80, 0, false, false, null),
            createProduct("健身护腕", "专业健身护腕", 150, null, null, "PRODUCT", "🤝", "运动装备", 200, 0, false, false, null),
            createProduct("速干T恤", "透气速干运动T恤", 250, null, null, "PRODUCT", "👕", "运动装备", 120, 0, false, false, null),
            createProduct("VIP专属-蛋白粉", "高蛋白健身蛋白粉", 500, null, null, "PRODUCT", "💪", "VIP专属", 50, 0, true, false, null),
            createProduct("VIP专属-智能手环", "运动智能手环", 800, null, null, "PRODUCT", "⌚", "VIP专属", 30, 0, true, false, null),
            createProduct("免费私教课", "价值200元的一对一私教课1节", 1000, null, null, "SERVICE", "🎯", "会员服务", 20, 0, false, true, 7),
            createProduct("免费团课体验", "任意团课体验1次", 300, null, null, "SERVICE", "📝", "会员服务", 50, 0, false, false, 14),
            createProduct("会员积分翻倍", "购买后7天内签到积分翻倍", 400, null, null, "PRIVILEGE", "✨", "会员特权", 100, 0, false, false, 7)
        );

        pointsProductRepository.saveAll(products);
    }

    private PointsProduct createProduct(String name, String description, int pointsCost, 
                                       Integer minSpend, Integer discountAmount, String discountType,
                                       String icon, String category, int stock, int sold, boolean isVip,
                                       boolean isActive, Integer expireDays) {
        PointsProduct product = new PointsProduct();
        product.setName(name);
        product.setDescription(description);
        product.setPointsCost(pointsCost);
        product.setMinSpend(minSpend);
        product.setDiscountAmount(discountAmount);
        product.setDiscountType(discountType);
        product.setIcon(icon);
        product.setCategory(category);
        product.setStockQuantity(stock);
        product.setSoldCount(sold);
        product.setIsVip(isVip);
        product.setIsActive(isActive == false ? false : true);
        product.setExpireDays(expireDays);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
}
