package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.AdminCreateRequest;
import com.example.vietstage_web_be.dto.request.InstructorCreateRequest;
import com.example.vietstage_web_be.dto.request.ReviewActionRequest;
import com.example.vietstage_web_be.dto.request.UserRoleUpdateRequest;
import com.example.vietstage_web_be.dto.request.UserStatusUpdateRequest;
import com.example.vietstage_web_be.dto.response.AdminCreateResponse;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.DashboardStatsResponse;
import com.example.vietstage_web_be.dto.response.InstructorCreateResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.AdminUserResponse;
import com.example.vietstage_web_be.dto.response.ReviewItemResponse;
import com.example.vietstage_web_be.service.IAdminUserService;
import com.example.vietstage_web_be.service.IAdminReviewService;
import com.example.vietstage_web_be.service.IAdminDashboardService;
import com.example.vietstage_web_be.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/dashboard")
    public ApiResponse<DashboardStatsResponse> getDashboard() {
        return ApiResponse.<DashboardStatsResponse>builder()
                .success(true)
                .message("Success")
                .data(adminDashboardService.getDashboardStats())
                .build();
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) List<String> roles,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.<PageResponse<AdminUserResponse>>builder()
                .success(true)
                .message("Successfully fetched all users")
                .data(adminUserService.getAllUsers(page, size, search, roles, status, sortBy, sortDir))
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

    @PutMapping("/users/{id}/role")
    public ApiResponse<Void> updateUserRole(
            @AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest request) {
        
        adminUserService.updateUserRole(id, request.getRole(), currentUser.getId());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully updated user role")
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
    public ApiResponse<Void> approveReview(
            @AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User currentUser,
            @PathVariable Long id) {
        adminReviewService.approveReview(id, currentUser.getId());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully approved review")
                .build();
    }

    @PostMapping("/reviews/{id}/reject")
    public ApiResponse<Void> rejectReview(
            @AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User currentUser,
            @PathVariable Long id, 
            @RequestBody ReviewActionRequest request) {
        adminReviewService.rejectReview(id, request.getFeedback(), currentUser.getId());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully rejected review")
                .build();
    }
}
