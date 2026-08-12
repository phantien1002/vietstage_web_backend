package com.example.vietstage_web_be.controller;

import com.cloudinary.Api;
import com.example.vietstage_web_be.dto.request.LearnerQuizRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.LearnerForInstructorResponse;
import com.example.vietstage_web_be.dto.response.LearnerQuizProgressResponse;
import com.example.vietstage_web_be.security.CustomUserDetails;
import com.example.vietstage_web_be.service.IInstructorService;
import com.example.vietstage_web_be.service.IQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
@Tag(name = "Progress", description = "Các API quản lý Tiến độ học tập")
public class InstructorController {

    private final IInstructorService instructorService;
    private final IQuizService quizService;

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

    @PostMapping("/quizzes")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<LearnerQuizProgressResponse>> createQuiz(@Valid @RequestBody LearnerQuizRequest request){
        LearnerQuizProgressResponse response = quizService.createQuizByLearnerLever(request);

        return ResponseEntity.ok(
                ApiResponse.<LearnerQuizProgressResponse>builder()
                        .success(true)
                        .message("Learner quiz has been created successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/quizzes/{quizId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<LearnerQuizProgressResponse>> updateQuiz(@PathVariable Long quizId,
                                                                               @Valid @RequestBody LearnerQuizRequest request){
        LearnerQuizProgressResponse response = quizService.updateQuizByLearnerLevel(quizId, request);

        return ResponseEntity.ok(
                ApiResponse.<LearnerQuizProgressResponse>builder()
                        .success(true)
                        .message("Learner quiz has been updated successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/quizzes/{levelId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<LearnerQuizProgressResponse>>> getQuiz(@PathVariable Long learnerId, Long instrumentId,  Long skillLevelId){
        List<LearnerQuizProgressResponse> responses = quizService.getQuizzesByLearnerLevel(learnerId, instrumentId, skillLevelId);

        return ResponseEntity.ok(
                ApiResponse.<List<LearnerQuizProgressResponse>>builder()
                        .success(true)
                        .message("Learner quiz has been updated successfully")
                        .data(responses)
                        .build()
        );
    }
}
