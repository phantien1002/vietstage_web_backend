package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.AdminCreateRequest;
import com.example.vietstage_web_be.dto.request.AdminUserUpdateRequest;
import com.example.vietstage_web_be.dto.request.InstructorCreateRequest;
import com.example.vietstage_web_be.dto.request.ReviewActionRequest;
import com.example.vietstage_web_be.dto.request.UserStatusUpdateRequest;
import com.example.vietstage_web_be.dto.response.AdminCreateResponse;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.DashboardStatsResponse;
import com.example.vietstage_web_be.dto.response.InstructorCreateResponse;
import com.example.vietstage_web_be.dto.response.AdminUserResponse;
import com.example.vietstage_web_be.dto.response.ReviewItemResponse;
import com.example.vietstage_web_be.service.IAdminUserService;
import com.example.vietstage_web_be.service.IAdminReviewService;
import com.example.vietstage_web_be.service.IAdminDashboardService;
import com.example.vietstage_web_be.service.IUserService;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Các API quản trị hệ thống")
public class AdminController {
    private final IUserService userService;
    private final IAdminUserService adminUserService;
    private final IAdminReviewService adminReviewService;
    private final IAdminDashboardService adminDashboardService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardStatsResponse> getDashboard() {
        return ApiResponse.<DashboardStatsResponse>builder()
                .success(true)
                .message("Success")
                .data(adminDashboardService.getDashboardStats())
                .build();
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserResponse>> getAllUsers() {
        return ApiResponse.<List<AdminUserResponse>>builder()
                .success(true)
                .message("Successfully fetched all users")
                .data(adminUserService.getAllUsers())
                .build();
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusUpdateRequest request) {
        
        adminUserService.updateUserStatus(id, request.getStatus());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully updated user status")
                .build();
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        adminUserService.updateUser(id, request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully updated user")
                .build();
    }

    @PostMapping("/create-instructor")
    public ApiResponse<InstructorCreateResponse> createInstructor(
            @Valid @RequestBody InstructorCreateRequest request
    ){
        return ApiResponse.<InstructorCreateResponse>builder()
                .message("Successfully created instructor")
                .data(userService.createInstructor(request))
                .build();
    }

    @PostMapping("/create-admin")
    public ApiResponse<AdminCreateResponse> createAdmin(
            @Valid @RequestBody AdminCreateRequest request) {

        return ApiResponse.<AdminCreateResponse>builder()
                .message("Tạo tài khoản Admin thành công")
                .data(userService.createAdmin(request))
                .build();
    }

    @GetMapping("/reviews")
    public ApiResponse<List<ReviewItemResponse>> getAllReviews() {
        return ApiResponse.<List<ReviewItemResponse>>builder()
                .success(true)
                .message("Successfully fetched all reviews")
                .data(adminReviewService.getAllReviews())
                .build();
    }

    @PostMapping("/reviews/{id}/approve")
    public ApiResponse<Void> approveReview(@PathVariable Long id) {
        adminReviewService.approveReview(id, getCurrentAdminId());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully approved review")
                .build();
    }

    @PostMapping("/reviews/{id}/pending")
    public ApiResponse<Void> resetReview(@PathVariable Long id) {
        adminReviewService.resetReview(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully reset review to pending")
                .build();
    }

    @PostMapping("/reviews/{id}/reject")
    public ApiResponse<Void> rejectReview(
            @PathVariable Long id, 
            @RequestBody ReviewActionRequest request) {
        adminReviewService.rejectReview(id, request.getFeedback(), getCurrentAdminId());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully rejected review")
                .build();
    }

    private Long getCurrentAdminId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
