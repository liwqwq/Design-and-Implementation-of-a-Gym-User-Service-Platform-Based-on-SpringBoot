package com.gym.service.entity;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "personal_trainers")
public class PersonalTrainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialty;

    private String bio;

    private String certifications;

    private Double rating;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String schedule;
}
