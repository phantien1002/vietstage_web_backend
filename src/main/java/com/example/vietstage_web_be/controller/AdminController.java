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
import com.example.vietstage_web_be.dto.response.ApiResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
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
    public ApiResponse<DashboardStatsResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.OffsetDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.OffsetDateTime toDate,
            @RequestParam(defaultValue = "MONTH") String granularity) {
        
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new com.example.vietstage_web_be.exception.AppException(com.example.vietstage_web_be.exception.ErrorCode.BAD_REQUEST, "Từ ngày không được lớn hơn Đến ngày");
        }
        
        if (!"DAY".equalsIgnoreCase(granularity) && !"WEEK".equalsIgnoreCase(granularity) && !"MONTH".equalsIgnoreCase(granularity)) {
            throw new com.example.vietstage_web_be.exception.AppException(com.example.vietstage_web_be.exception.ErrorCode.BAD_REQUEST, "Granularity chỉ chấp nhận DAY, WEEK, hoặc MONTH");
        }

        java.time.ZoneId zoneId = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime from = fromDate != null ? fromDate.atZoneSameInstant(zoneId).toLocalDateTime() : null;
        LocalDateTime to = toDate != null ? toDate.atZoneSameInstant(zoneId).toLocalDateTime() : null;

        return ApiResponse.<DashboardStatsResponse>builder()
                .success(true)
                .message("Success")
                .data(adminDashboardService.getDashboardStats(from, to, granularity.toUpperCase()))
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
            @AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User currentUser,
            @PathVariable Long id,
            @RequestBody UserStatusUpdateRequest request) {
        
        adminUserService.updateUserStatus(id, request.getStatus(), currentUser.getId());
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

    @PutMapping("/users/{id}")
    public ApiResponse<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody com.example.vietstage_web_be.dto.request.UpdateProfileRequest request) {
        
        adminUserService.updateUser(id, request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully updated user profile")
                .build();
    }

    @PostMapping("/users/{id}/reset-password")
    public ApiResponse<Void> resetPassword(
            @AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User currentUser,
            @PathVariable Long id,
            @Valid @RequestBody com.example.vietstage_web_be.dto.request.AdminResetPasswordRequest request) {
        
        String newPassword = request.getNewPassword();
        adminUserService.resetPassword(id, newPassword, currentUser.getId());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully reset password")
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



    @GetMapping("/reviews")
    public ApiResponse<PageResponse<ReviewItemResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false) Long instrumentId) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        
        return ApiResponse.<PageResponse<ReviewItemResponse>>builder()
                .success(true)
                .message("Successfully fetched all reviews")
                .data(adminReviewService.getAllReviews(status, search, instructorId, instrumentId, pageable))
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
            @Valid @RequestBody ReviewActionRequest request) {
        adminReviewService.rejectReview(id, request.getFeedback(), currentUser.getId());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully rejected review")
                .build();
    }
}
