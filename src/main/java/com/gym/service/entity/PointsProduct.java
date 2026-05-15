package com.gym.service.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "points_products")
public class PointsProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    @Column(nullable = false)
    private String description;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "points_cost", nullable = false)
    private Integer pointsCost;

    @Column(name = "min_spend")
    private Integer minSpend;

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @Column(name = "discount_type")
    private String discountType;

    @Column
    private String icon;

    @Column
    private String category;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "sold_count")
    private Integer soldCount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_vip")
    private Boolean isVip;

    @Column(name = "expire_days")
    private Integer expireDays;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
