package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.CreateInstructorRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.CreateInstructorResponse;
import com.example.vietstage_web_be.service.IInstructorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "APIs for administration and statistics")
public class AdminController {
    private final IInstructorService instructorService;

    @GetMapping("/dashboard")
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Welcome to the Admin Dashboard! You have ADMIN privileges.");
    }

    @PostMapping("/create-instructor")
    public ResponseEntity<ApiResponse<CreateInstructorResponse>> createInstructorByAdmin(
            @Valid @RequestBody CreateInstructorRequest request) {
        CreateInstructorResponse response = instructorService.createInstructor(request);

        return ResponseEntity.ok(
                ApiResponse.<CreateInstructorResponse>builder()
                        .message("Instructor created successfully")
                        .data(response)
                        .build()
        );
    }
}
