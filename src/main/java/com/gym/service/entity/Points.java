package com.gym.service.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "points")
public class Points {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer totalPoints;

    @Column(nullable = false)
    private Integer availablePoints;

    @Column(nullable = false)
    private Integer usedPoints;

    @Column(name = "last_checkin_date")
    private LocalDateTime lastCheckinDate;

    @Column(name = "consecutive_days")
    private Integer consecutiveDays;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
