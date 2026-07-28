package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.*;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.AuthResponse;
import com.example.vietstage_web_be.dto.response.UserResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.security.JwtTokenProvider;
import com.example.vietstage_web_be.service.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {
    private final IAuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository UserRepository;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AuthResponse>> register(@RequestBody @Valid RegisterRequest request){
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(BaseResponse.<AuthResponse>builder()
                .success(true)
                .message("Register initiated successfully")
                .data(response)
                .build());
    }

    @PostMapping("/verify-registration")
    public ResponseEntity<BaseResponse<AuthResponse>> verifyRegistration(@RequestBody @Valid VerifyRegistrationRequest request){
        AuthResponse response = authService.verifyRegistration(request);
        return ResponseEntity.ok(BaseResponse.<AuthResponse>builder()
                .success(true)
                .message("Account verified successfully")
                .data(response)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request){
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(BaseResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successfully")
                .data(response)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<AuthResponse>> refresh(@RequestBody @Valid RefreshRequest request){
        AuthResponse response = authService.refresh(request.getSessionId(), request.getRefreshToken());
        return ResponseEntity.ok(BaseResponse.<AuthResponse>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(response)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            String sessionId = jwtTokenProvider.getSessionIdFromToken(token);
            if (sessionId != null) {
                authService.logout(sessionId);
            }
        }
        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .success(true)
                .message("Logout successfully")
                .build());
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request){
        authService.forgotPassword(request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Verification code sent to email successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Password has been reset successfully! You can login now.")
                .build();

        return ResponseEntity.ok(response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
