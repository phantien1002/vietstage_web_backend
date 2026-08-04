package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.response.*;
import com.example.vietstage_web_be.service.IInstructorService;
import com.example.vietstage_web_be.service.ILearnerProgressService;
import com.google.api.gax.paging.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Progress", description = "Các API quản lý Tiến độ học tập")
public class ProgressController {
    private final ILearnerProgressService progressService;
    private final IInstructorService instructorService;

    @GetMapping("/users/me/progress")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<List<LearnerProgressItemResponse>>> getLearnerProgress(
            @RequestParam(value = "instrument_id", required = false) Long instrumentId,
            @RequestParam(value = "skill_level_id", required = false) Long skillLevelId,
            Authentication authentication) {

        Long currentLearnerId = Long.parseLong(authentication.getName());

        List<LearnerProgressItemResponse> data = progressService.getLearnerProgress(
                currentLearnerId, instrumentId, skillLevelId);

        return ResponseEntity.ok(ApiResponse.<List<LearnerProgressItemResponse>>builder()
                .message("Get learner progress successfully")
                .data(data)
                .build());
    }

    @GetMapping("/users/me/progress/summary")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<LearnerProgressSummaryResponse>> getLearnerProgressSummary(
            Authentication authentication) {

        Long currentLearnerId = Long.parseLong(authentication.getName());
        LearnerProgressSummaryResponse data = progressService.getLearnerProgressSummary(currentLearnerId);

        return ResponseEntity.ok(ApiResponse.<LearnerProgressSummaryResponse>builder()
                .message("Get learner progress summary successfully")
                .data(data)
                .build());
    }

    @GetMapping("/lessons/{id}/learners/{learner_id}/progress")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<InstructorLearnerProgressResponse>> getLearnerProgressByInstructor(
            @PathVariable("id") Long lessonId,
            @PathVariable("learner_id") Long learnerId,
            Authentication authentication) {

        Long currentInstructorId = Long.parseLong(authentication.getName());
        InstructorLearnerProgressResponse data = progressService.getLearnerProgressByInstructor(
                lessonId, learnerId, currentInstructorId);

        return ResponseEntity.ok(ApiResponse.<InstructorLearnerProgressResponse>builder()
                .message("Get instructor learner progress successfully")
                .data(data)
                .build());
    }

    @GetMapping("/instructor/learners/{learnerId}/progress/summary")
    public ResponseEntity<LearnerSummaryResponse> getLearnerProgressSummary(@PathVariable("learnerId") Long learnerId) {

        LearnerSummaryResponse response = instructorService.getLearnerProgressSummary(learnerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/instructor/practice-attempts")
    public ResponseEntity<?> getPracticeAttempts(@ModelAttribute PracticeAttemptGroupedResponse filterRequest) {

        if (filterRequest.getTimeGroup() != null && !filterRequest.getTimeGroup().isBlank()) {
            List<PracticeAttemptGroupedResponse> groupedResponse = instructorService.getGroupedPracticeAttemptDetail(filterRequest);
            return ResponseEntity.ok(groupedResponse);
        }

        //Page<PracticeAttemptDetailResponse> detailResponsePage = instructorService.getFilteredPracticeAttemptDetail(filterRequest);
        Page<PracticeAttemptDetailResponse> detailResponsePage = null;
        return ResponseEntity.ok(detailResponsePage);
    }
}

