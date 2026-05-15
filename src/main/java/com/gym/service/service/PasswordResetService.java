package com.gym.service.service;

public interface PasswordResetService {
    void requestPasswordReset(String email, String userType);
    
    boolean validateToken(String token);
    
    void resetPassword(String token, String newPassword);
    
    String getUserTypeByToken(String token);
}
