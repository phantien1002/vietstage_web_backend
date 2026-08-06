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

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
public class InstructorController {

    private final IInstructorService instructorService;

    @GetMapping("/learners")
    @PreAuthorize("hasRole('INSTRUCTOR')")
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
