package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.ForgotPasswordRequest;
import com.example.vietstage_web_be.dto.request.LoginRequest;
import com.example.vietstage_web_be.dto.request.RegisterRequest;
import com.example.vietstage_web_be.dto.request.ResetPasswordRequest;
import com.example.vietstage_web_be.dto.response.AuthResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.repository.RoleRepository;
import com.example.vietstage_web_be.entity.Role;
import com.example.vietstage_web_be.security.JwtTokenProvider;
import com.example.vietstage_web_be.service.IAuthService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final UserRepository UserRepository;
    private final RoleRepository RoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthSessionService authSessionService;

    private final Map<String, String> tokenCache = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (UserRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXIST, "Email exist");
        }

        Role learnerRole = RoleRepository.findByName("LEARNER")
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Role LEARNER not found"));

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(learnerRole)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserRepository.save(user);

        return AuthResponse.builder().message("Register successfully!").build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = UserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND, "Email not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new AppException(ErrorCode.PASSWORD_INCORRECT, "Password not match");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED, "Account locked");
        }

        String roleName = user.getRole().getName();
        String sessionId = UUID.randomUUID().toString();
        
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), roleName, sessionId);
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        authSessionService.createSession(sessionId, user.getId(), refreshToken);

        return AuthResponse.builder()
                .message("Login successfully")
                .token(accessToken)
                .refreshToken(refreshToken)
                .sessionId(sessionId)
                .role(roleName)
                .build();
    }

    @Override
    public AuthResponse refresh(String sessionId, String refreshToken) {
        if (sessionId == null || refreshToken == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Missing session ID or refresh token");
        }

        Long userId = authSessionService.validateSessionAndGetUserId(sessionId, refreshToken);
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Invalid or expired session");
        }

        User user = UserRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED, "Account locked");
        }

        String roleName = user.getRole().getName();
        String newSessionId = UUID.randomUUID().toString();
        
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), roleName, newSessionId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken();

        // Revoke old session and create a new one (Session Rotation)
        authSessionService.revokeSession(sessionId);
        authSessionService.createSession(newSessionId, user.getId(), newRefreshToken);

        return AuthResponse.builder()
                .message("Token refreshed successfully")
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .sessionId(newSessionId)
                .role(roleName)
                .build();
    }

    @Override
    public void logout(String sessionId) {
        if (sessionId != null) {
            authSessionService.revokeSession(sessionId);
        }
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = UserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Email does not exist"));

        String verificationCode = String.valueOf(new Random().nextInt(900000) + 100000);

        this.tokenCache.put(request.getEmail(), verificationCode);

        return verificationCode;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!this.tokenCache.containsKey(request.getEmail())) {
            throw new AppException(ErrorCode.INVALID_VERIFICATION_CODE, "No verification process initiated for this email");
        }

        String savedCode = this.tokenCache.get(request.getEmail());
        if (!savedCode.equals(request.getVerificationCode())) {
            throw new AppException(ErrorCode.INVALID_VERIFICATION_CODE, "Verification code is incorrect");
        }

        User user = UserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        UserRepository.save(user);

        this.tokenCache.remove(request.getEmail());
    }
}
