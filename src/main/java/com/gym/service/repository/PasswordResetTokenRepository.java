package com.gym.service.repository;

import com.gym.service.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    
    List<PasswordResetToken> findByUserId(Long userId);
    List<PasswordResetToken> findByCoachId(Long coachId);
    List<PasswordResetToken> findByAdminId(Long adminId);
    
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
    
    void deleteByUserId(Long userId);
    void deleteByCoachId(Long coachId);
    void deleteByAdminId(Long adminId);
}
