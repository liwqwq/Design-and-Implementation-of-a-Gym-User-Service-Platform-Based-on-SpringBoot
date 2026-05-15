package com.gym.service.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "badges")
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    private String description;

    private String icon;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(nullable = false)
    private Boolean unlocked;

    @Column(name = "required_days")
    private Integer requiredDays;
}
