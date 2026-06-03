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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "APIs for user management")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    // ----------------------------------------------------------------
    // GET /api/users/{id} — ADMIN
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "ID của user") @PathVariable Long id) {

        UserResponse data = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .message("User retrieved successfully")
                .data(data)
                .build());
    }

    // ----------------------------------------------------------------
    // GET /api/users — ADMIN
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

            @Parameter(description = "Số trang, bắt đầu từ 1 (mặc định: 1)")
            @RequestParam(defaultValue = "1") int pageNumber,

            @Parameter(description = "Số phần tử mỗi trang, tối đa 100 (mặc định: 10)")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "Sắp xếp theo: id | email | fullName | role | createdAt (mặc định: id)")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "true = giảm dần (Z→A, mới→cũ), false = tăng dần (mặc định: false)")
            @RequestParam(defaultValue = "false") boolean sortDescending
    ) {
        PageResponse<UserResponse> data = userService.getUsers(
                keyword, role, isActive, pageNumber, pageSize, sortBy, sortDescending);
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

            @Parameter(description = "Số trang, bắt đầu từ 1 (mặc định: 1)")
            @RequestParam(defaultValue = "1") int pageNumber,

            @Parameter(description = "Số phần tử mỗi trang, tối đa 100 (mặc định: 10)")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "Sắp xếp theo: id | email | fullName | createdAt (mặc định: id)")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "true = giảm dần (Z→A, mới→cũ), false = tăng dần (mặc định: false)")
            @RequestParam(defaultValue = "false") boolean sortDescending
    ) {
        PageResponse<UserResponse> data = userService.getLearners(
                keyword, isActive, pageNumber, pageSize, sortBy, sortDescending);
        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .message("Learners retrieved successfully")
                .data(data)
                .build());
    }

    // ----------------------------------------------------------------
    // GET /api/users/instructors — ADMIN
    // ----------------------------------------------------------------
    @GetMapping("/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all instructors with search, sort and paging (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getInstructors(
            @Parameter(description = "Tìm kiếm theo email hoặc fullName")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Lọc theo trạng thái: true = active, false = inactive")
            @RequestParam(required = false) Boolean isActive,

            @Parameter(description = "Số trang, bắt đầu từ 1 (mặc định: 1)")
            @RequestParam(defaultValue = "1") int pageNumber,

            @Parameter(description = "Số phần tử mỗi trang, tối đa 100 (mặc định: 10)")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "Sắp xếp theo: id | email | fullName | createdAt (mặc định: id)")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "true = giảm dần (Z→A, mới→cũ), false = tăng dần (mặc định: false)")
            @RequestParam(defaultValue = "false") boolean sortDescending
    ) {
        PageResponse<UserResponse> data = userService.getInstructors(
                keyword, isActive, pageNumber, pageSize, sortBy, sortDescending);
        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .message("Instructors retrieved successfully")
                .data(data)
                .build());
    }
}
