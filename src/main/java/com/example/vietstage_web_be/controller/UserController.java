package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.UserResponse;
import com.example.vietstage_web_be.service.IUserService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final IUserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse data = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .message("User retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "false") boolean sortDescending) {
        PageResponse<UserResponse> data = userService.getUsers(
                keyword, role, isActive, pageNumber, pageSize, sortBy, sortDescending);
        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .message("User list retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/learners")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getLearners(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "false") boolean sortDescending) {
        PageResponse<UserResponse> data = userService.getLearners(
                keyword, isActive, pageNumber, pageSize, sortBy, sortDescending);
        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .message("Learners retrieved successfully")
                .data(data)
                .build());
    }

    @GetMapping("/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getInstructors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "false") boolean sortDescending) {
        PageResponse<UserResponse> data = userService.getInstructors(
                keyword, isActive, pageNumber, pageSize, sortBy, sortDescending);
        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .message("Instructors retrieved successfully")
                .data(data)
                .build());
    }
}
