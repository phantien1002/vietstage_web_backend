package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.UserResponse;
import com.example.vietstage_web_be.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "APIs for user profile management")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<String> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok("Hello! Your email is: " + userDetails.getUsername()
                + ", roles: " + userDetails.getAuthorities());
    }

    // ----------------------------------------------------------------
    // GET /api/users — Chỉ ADMIN
    // ----------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users with search, sort and paging (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @Parameter(description = "Tìm kiếm theo email hoặc fullName")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Lọc theo role: ADMIN | INSTRUCTOR | LEARNER")
            @RequestParam(required = false) String role,

            @Parameter(description = "Lọc theo trạng thái: true = active, false = inactive")
            @RequestParam(required = false) Boolean isActive,

            @Parameter(description = "Số trang (bắt đầu từ 0, mặc định: 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số phần tử mỗi trang (mặc định: 10)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sắp xếp theo field: id | email | fullName | role | createdAt (mặc định: id)")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Chiều sắp xếp: asc | desc (mặc định: asc)")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PageResponse<UserResponse> data = userService.getUsers(keyword, role, isActive, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .message("Users retrieved successfully")
                .data(data)
                .build());
    }

    // ----------------------------------------------------------------
    // GET /api/users/learners — ADMIN hoặc INSTRUCTOR
    // ----------------------------------------------------------------
    @GetMapping("/learners")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Get all learners with search, sort and paging (Admin & Instructor)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getLearners(
            @Parameter(description = "Tìm kiếm theo email hoặc fullName")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Lọc theo trạng thái: true = active, false = inactive")
            @RequestParam(required = false) Boolean isActive,

            @Parameter(description = "Số trang (bắt đầu từ 0, mặc định: 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số phần tử mỗi trang (mặc định: 10)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sắp xếp theo field: id | email | fullName | createdAt (mặc định: id)")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Chiều sắp xếp: asc | desc (mặc định: asc)")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PageResponse<UserResponse> data = userService.getLearners(keyword, isActive, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .message("Learners retrieved successfully")
                .data(data)
                .build());
    }
}
