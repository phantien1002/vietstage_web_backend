package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.CreateInstructorRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.CreateInstructorResponse;
import com.example.vietstage_web_be.service.IInstructorService;
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
    private final IInstructorService instructorService;

    @GetMapping("/dashboard")
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Welcome to the Admin Dashboard! You have ADMIN privileges.");
    }

    @PostMapping("/create-instructor")
    public ResponseEntity<ApiResponse<CreateInstructorResponse>> createInstructor(
            @RequestBody @Valid CreateInstructorRequest request) {
        CreateInstructorResponse data = instructorService.createInstructorAccount(request);

        ApiResponse<CreateInstructorResponse> response = ApiResponse.<CreateInstructorResponse>builder()
                .message("Instructor created successfully")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }
}
