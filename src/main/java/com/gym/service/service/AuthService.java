package com.gym.service.service;

import com.gym.service.entity.User;

public interface AuthService {
    User register(User user);
    String login(String username, String password);
}