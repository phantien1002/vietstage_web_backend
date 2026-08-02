package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.ChangePasswordRequest;
import com.example.vietstage_web_be.dto.request.UpdateProfileRequest;
import com.example.vietstage_web_be.dto.request.UpdateUserStatusRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.UserResponse;
import com.example.vietstage_web_be.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.vietstage_web_be.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Các API liên quan đến tài khoản cá nhân")
public class UserController {

    private final IUserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse data = userService.getMyProfile(email);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .message("Profile retrieved successfully")
                .data(data)
                .build());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse data = userService.updateMyProfile(email, request);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .message("Profile updated successfully")
                .data(data)
                .build());
    }

    @PutMapping("/me/fcm-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestBody Map<String, String> request) {
        
        String fcmToken = request.get("fcm_token");
        userService.updateFcmToken(user.getId(), fcmToken);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("FCM Token updated successfully")
                .build());
    }
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        userService.changePassword(user.getId(), request);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Đổi mật khẩu thành công")
                .build());
    }

}

