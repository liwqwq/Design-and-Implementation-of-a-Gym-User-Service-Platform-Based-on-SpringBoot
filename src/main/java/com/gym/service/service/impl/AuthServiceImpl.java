package com.gym.service.service.impl;

import com.gym.service.entity.User;
import com.gym.service.repository.UserRepository;
import com.gym.service.service.AuthService;
import com.gym.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User register(User user) {
        // 验证邮箱（必填）
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        
        // 验证邮箱唯一性
        userRepository.findByEmail(user.getEmail())
                .ifPresent(existingUser -> {
                    throw new RuntimeException("该邮箱已被注册");
                });
        
        // 验证手机号唯一性
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            userRepository.findByPhone(user.getPhone())
                    .ifPresent(existingUser -> {
                        throw new RuntimeException("手机号已被注册");
                    });
        }
        
        // 验证密码强度
        if (!isValidPassword(user.getPassword())) {
            throw new RuntimeException("密码长度至少为6位");
        }
        
        // 使用BCrypt加密密码，强度10轮
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        user.setRole("USER");
        return userRepository.save(user);
    }

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.isActive()) {
            throw new RuntimeException("User is not active");
        }

        String role = user.getRole();
        boolean normalUserRole = role == null
                || "USER".equalsIgnoreCase(role)
                || "MEMBER".equalsIgnoreCase(role)
                || "VIP".equalsIgnoreCase(role)
                || "会员".equals(role)
                || "VIP会员".equals(role);
        if (!normalUserRole) {
            throw new RuntimeException("Invalid credentials");
        }

        String storedPassword = user.getPassword();
        boolean passwordMatched = storedPassword != null && passwordEncoder.matches(password, storedPassword);
        // 兼容早期测试数据里可能出现的明文密码；匹配后立即升级为 BCrypt。
        if (!passwordMatched && Objects.equals(password, storedPassword)) {
            user.setPassword(passwordEncoder.encode(password));
            passwordMatched = true;
        }
        if (!passwordMatched) {
            throw new RuntimeException("Invalid password");
        }

        if (!"USER".equals(role)) {
            user.setRole("USER");
        }
        userRepository.save(user);
        return jwtUtil.generateToken(user.getUsername());
    }
    
    // 验证密码强度
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        return true;
    }
}