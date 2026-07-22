package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.ForgotPasswordRequest;
import com.example.vietstage_web_be.dto.request.LoginRequest;
import com.example.vietstage_web_be.dto.request.RegisterRequest;
import com.example.vietstage_web_be.dto.request.ResetPasswordRequest;
import com.example.vietstage_web_be.dto.response.AuthResponse;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String sessionId, String refreshToken);
    void logout(String sessionId);
    String forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
