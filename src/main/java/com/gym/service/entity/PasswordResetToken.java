package com.gym.service.entity;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "coach_id")
    private Long coachId;
    
    @Column(name = "admin_id")
    private Long adminId;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean used;
    
    @Column(nullable = false)
    private String userType; // USER, COACH, ADMIN
}
