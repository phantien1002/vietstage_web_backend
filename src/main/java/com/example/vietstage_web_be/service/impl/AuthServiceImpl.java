package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.ForgotPasswordRequest;
import com.example.vietstage_web_be.dto.request.LoginRequest;
import com.example.vietstage_web_be.dto.request.RegisterRequest;
import com.example.vietstage_web_be.dto.request.ResetPasswordRequest;
import com.example.vietstage_web_be.dto.response.AuthResponse;
import com.example.vietstage_web_be.entity.UserProfiles;
import com.example.vietstage_web_be.entity.Users;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.UsersRepository;
import com.example.vietstage_web_be.security.JwtTokenProvider;
import com.example.vietstage_web_be.service.IAuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider  jwtTokenProvider;

    private final Map<String, String> tokenCache = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usersRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXIST, "Email exist");
        }

        Users user = Users.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("LEARNER")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        usersRepository.save(user);

        UserProfiles profile = UserProfiles.builder()
                .user(user)
                .fullName(request.getFullName())
                .createdAt(LocalDateTime.now())
                .build();

        user.setUserProfiles(profile);
        usersRepository.save(user);

        return AuthResponse.builder().message("Register successfully!").build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND, "Email not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH, "Password not match");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED, "Account locked");
        }

        String token = jwtTokenProvider.generateLoginToken(user.getEmail());

        return AuthResponse.builder().message("Login successfully")
                .token(token)
                .build();
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request) {
        Users user = usersRepository.findByEmail(request.getEmail())
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

        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);

        this.tokenCache.remove(request.getEmail());
    }
}
