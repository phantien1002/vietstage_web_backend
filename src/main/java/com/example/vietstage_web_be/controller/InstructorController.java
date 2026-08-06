package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.LearnerForInstructorResponse;
import com.example.vietstage_web_be.security.CustomUserDetails;
import com.example.vietstage_web_be.service.IInstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
@Tag(name = "Instructor", description = "Các API dành riêng cho Giảng viên")
public class InstructorController {

    private final IInstructorService instructorService;

    @GetMapping("/learners")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Lấy danh sách Học viên", description = "API lấy danh sách các học viên mà Giảng viên hiện tại được phép theo dõi.")
    public ResponseEntity<ApiResponse<Page<LearnerForInstructorResponse>>> getLearnersForInstructor(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LearnerForInstructorResponse> result = instructorService.getLearnersForInstructor(userDetails.getUser().getId(), search, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.<Page<LearnerForInstructorResponse>>builder()
                .success(true)
                .message("Fetched learners successfully")
                .data(result)
                .build()
        );
    }
}
