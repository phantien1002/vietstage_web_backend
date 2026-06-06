package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.ForgotPasswordRequest;
import com.example.vietstage_web_be.dto.request.LoginRequest;
import com.example.vietstage_web_be.dto.request.RegisterRequest;
import com.example.vietstage_web_be.dto.request.ResetPasswordRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.AuthResponse;
import com.example.vietstage_web_be.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request){
        AuthResponse response = authService.register(request);
        if (response.getMessage().contains("successfully")) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        AuthResponse response = authService.login(request);
        if (response.getToken() != null){
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request){
        String code = authService.forgotPassword(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message("Verification code generated successfully")
                .data(code)
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
}
