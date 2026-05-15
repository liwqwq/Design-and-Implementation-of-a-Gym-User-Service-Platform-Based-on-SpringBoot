package com.gym.service.service.impl;

import com.gym.service.entity.Admin;
import com.gym.service.entity.Coach;
import com.gym.service.entity.PasswordResetToken;
import com.gym.service.entity.User;
import com.gym.service.repository.AdminRepository;
import com.gym.service.repository.CoachRepository;
import com.gym.service.repository.PasswordResetTokenRepository;
import com.gym.service.repository.UserRepository;
import com.gym.service.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Token有效期为1小时
    private static final int EXPIRATION_HOURS = 1;

    @Override
    @Transactional
    public void requestPasswordReset(String email, String userType) {
        String resetUrl = "";
        String username = "";
        
        if ("USER".equalsIgnoreCase(userType)) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("用户邮箱不存在"));
            username = user.getUsername();
            
            // 删除该用户之前的所有重置token
            tokenRepository.deleteByUserId(user.getId());

            // 生成新的重置token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUserId(user.getId());
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS));
            resetToken.setUsed(false);
            resetToken.setUserType("USER");
            tokenRepository.save(resetToken);
            
            resetUrl = "http://localhost:3000/user/reset-password?token=" + token;
        } else if ("COACH".equalsIgnoreCase(userType)) {
            Coach coach = coachRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("教练邮箱不存在"));
            username = coach.getUsername();
            
            // 删除该教练之前的所有重置token
            tokenRepository.deleteByCoachId(coach.getId());

            // 生成新的重置token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setCoachId(coach.getId());
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS));
            resetToken.setUsed(false);
            resetToken.setUserType("COACH");
            tokenRepository.save(resetToken);
            
            resetUrl = "http://localhost:3000/coach/reset-password?token=" + token;
        } else if ("ADMIN".equalsIgnoreCase(userType)) {
            Admin admin = adminRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("管理员邮箱不存在"));
            username = admin.getUsername();
            
            // 删除该管理员之前的所有重置token
            tokenRepository.deleteByAdminId(admin.getId());

            // 生成新的重置token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setAdminId(admin.getId());
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS));
            resetToken.setUsed(false);
            resetToken.setUserType("ADMIN");
            tokenRepository.save(resetToken);
            
            resetUrl = "http://localhost:3000/admin/reset-password?token=" + token;
        } else {
            throw new RuntimeException("无效的用户类型");
        }

        // 模拟发送重置邮件（实际项目中应使用邮件服务）
        System.out.println("===== 密码重置链接 =====");
        System.out.println("用户类型: " + userType);
        System.out.println("用户: " + username + " (" + email + ")");
        System.out.println("重置链接: " + resetUrl);
        System.out.println("有效期: " + EXPIRATION_HOURS + "小时");
        System.out.println("=========================");
    }

    @Override
    public boolean validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElse(null);
        
        return resetToken != null && !resetToken.isUsed() 
                && resetToken.getExpiryDate().isAfter(LocalDateTime.now());
    }

    @Override
    public String getUserTypeByToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("无效的token"));
        return resetToken.getUserType();
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("无效的token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token已被使用");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token已过期");
        }

        // 验证新密码强度
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("密码长度至少为6位");
        }

        String userType = resetToken.getUserType();
        
        if ("USER".equals(userType)) {
            User user = userRepository.findById(resetToken.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else if ("COACH".equals(userType)) {
            Coach coach = coachRepository.findById(resetToken.getCoachId())
                    .orElseThrow(() -> new RuntimeException("教练不存在"));
            coach.setPassword(passwordEncoder.encode(newPassword));
            coachRepository.save(coach);
        } else if ("ADMIN".equals(userType)) {
            Admin admin = adminRepository.findById(resetToken.getAdminId())
                    .orElseThrow(() -> new RuntimeException("管理员不存在"));
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminRepository.save(admin);
        }

        // 标记token已使用
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
