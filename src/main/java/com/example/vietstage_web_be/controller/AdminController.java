package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.AdminCreateRequest;
import com.example.vietstage_web_be.dto.request.InstructorCreateRequest;
import com.example.vietstage_web_be.dto.response.AdminCreateResponse;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.InstructorCreateResponse;
import com.example.vietstage_web_be.dto.response.AdminUserResponse;
import com.example.vietstage_web_be.service.IAdminUserService;
import com.example.vietstage_web_be.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {
    private final IUserService userService;
    private final IAdminUserService adminUserService;

    @GetMapping("/users")
    public ApiResponse<List<AdminUserResponse>> getAllUsers() {
        return ApiResponse.<List<AdminUserResponse>>builder()
                .message("Successfully fetched all users")
                .data(adminUserService.getAllUsers())
                .build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Welcome to the Admin Dashboard! You have ADMIN privileges.");
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
}
