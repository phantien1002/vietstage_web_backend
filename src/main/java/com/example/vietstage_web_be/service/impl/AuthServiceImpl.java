package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.ForgotPasswordRequest;
import com.example.vietstage_web_be.dto.request.LoginRequest;
import com.example.vietstage_web_be.dto.request.RegisterRequest;
import com.example.vietstage_web_be.dto.request.ResetPasswordRequest;
import com.example.vietstage_web_be.dto.request.VerifyRegistrationRequest;
import com.example.vietstage_web_be.dto.response.AuthResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.repository.RoleRepository;
import com.example.vietstage_web_be.entity.Role;
import com.example.vietstage_web_be.security.JwtTokenProvider;
import com.example.vietstage_web_be.service.IAuthService;
import com.example.vietstage_web_be.service.IEmailService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {
    private final UserRepository UserRepository;
    private final RoleRepository RoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthSessionService authSessionService;
    private final IEmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REGISTRATION_OTP_PREFIX = "REGISTRATION_OTP:";
    private static final String RESET_OTP_PREFIX = "RESET_OTP:";

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (UserRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXIST, "Email exist");
        }

        // Generate and send OTP
        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);
        
        java.util.Map<String, String> pendingUser = new java.util.HashMap<>();
        pendingUser.put("email", request.getEmail());
        pendingUser.put("password", passwordEncoder.encode(request.getPassword()));
        pendingUser.put("fullName", request.getFullName());
        pendingUser.put("otp", otpCode);

        redisTemplate.opsForValue().set(REGISTRATION_OTP_PREFIX + request.getEmail(), pendingUser, Duration.ofMinutes(5));
        
        try {
            emailService.sendOtpEmail(request.getEmail(), otpCode, "VietStage - Xác nhận tài khoản", "Mã xác nhận đăng ký tài khoản của bạn là:");
        } catch (Exception e) {
            log.error("Failed to send registration OTP email", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Cannot send OTP email");
        }

        return AuthResponse.builder()
                .message("Register initiated. Please check your email for OTP verification.")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse verifyRegistration(VerifyRegistrationRequest request) {
        java.util.Map<String, Object> pendingUser = (java.util.Map<String, Object>) redisTemplate.opsForValue().get(REGISTRATION_OTP_PREFIX + request.getEmail());
        
        if (pendingUser == null || !request.getOtpCode().equals(pendingUser.get("otp"))) {
            throw new AppException(ErrorCode.INVALID_VERIFICATION_CODE, "Invalid or expired OTP code");
        }

        Role learnerRole = RoleRepository.findByName("LEARNER")
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Role LEARNER not found"));

        String prefix = "HV"; // LEARNER
        Long nextId = UserRepository.findTopByOrderByIdDesc().map(User::getId).orElse(0L) + 1;
        String generatedUserCode = String.format("%s-%04d", prefix, nextId);

        User user = User.builder()
                .userCode(generatedUserCode)
                .email((String) pendingUser.get("email"))
                .passwordHash((String) pendingUser.get("password"))
                .fullName((String) pendingUser.get("fullName"))
                .role(learnerRole)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserRepository.save(user);

        redisTemplate.delete(REGISTRATION_OTP_PREFIX + request.getEmail());

        // Generate tokens
        String roleName = user.getRole().getName();
        String sessionId = UUID.randomUUID().toString();
        
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), roleName, sessionId);
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        authSessionService.createSession(sessionId, user.getId(), refreshToken);

        return AuthResponse.builder()
                .message("Account verified and logged in successfully")
                .token(accessToken)
                .refreshToken(refreshToken)
                .sessionId(sessionId)
                .userCode(user.getUserCode())
                .role(roleName)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = UserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND, "Email not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new AppException(ErrorCode.PASSWORD_INCORRECT, "Password not match");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED, "Account locked or not verified");
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
                .userCode(user.getUserCode())
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
                .userCode(user.getUserCode())
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
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = UserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Email does not exist"));

        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);

        redisTemplate.opsForValue().set(RESET_OTP_PREFIX + request.getEmail(), otpCode, Duration.ofMinutes(5));
        
        try {
            emailService.sendOtpEmail(request.getEmail(), otpCode, "VietStage - Yêu cầu Đổi Mật Khẩu", "Mã xác nhận đổi mật khẩu của bạn là:");
        } catch (Exception e) {
            log.error("Failed to send reset password OTP email", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Cannot send OTP email");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String savedCode = (String) redisTemplate.opsForValue().get(RESET_OTP_PREFIX + request.getEmail());
        
        if (savedCode == null || !savedCode.equals(request.getVerificationCode())) {
            throw new AppException(ErrorCode.INVALID_VERIFICATION_CODE, "Invalid or expired verification code");
        }

        User user = UserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        UserRepository.save(user);

        redisTemplate.delete(RESET_OTP_PREFIX + request.getEmail());
    }
}
