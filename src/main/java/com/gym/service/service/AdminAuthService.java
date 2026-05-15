package com.gym.service.service;

import com.gym.service.entity.Admin;

public interface AdminAuthService {
    String login(String username, String password);
    Admin getAdminByUsername(String username);
}
