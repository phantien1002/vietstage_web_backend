package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.InstructorCreateRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.InstructorCreateResponse;
import com.example.vietstage_web_be.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {
    private final IUserService userService;

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
}
