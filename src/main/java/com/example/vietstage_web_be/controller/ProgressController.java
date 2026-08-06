package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.InstructorPracticeAttemptRequest;
import com.example.vietstage_web_be.dto.response.*;
import com.example.vietstage_web_be.service.IInstructorService;
import com.example.vietstage_web_be.service.ILearnerProgressService;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.vietstage_web_be.entity.User;
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
            @AuthenticationPrincipal(expression = "user") User currentUser) {

        Long currentLearnerId = currentUser.getId();

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
            @AuthenticationPrincipal(expression = "user") User currentUser) {

        Long currentLearnerId = currentUser.getId();
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
            @AuthenticationPrincipal(expression = "user") User currentUser) {

        Long currentInstructorId = currentUser.getId();
        InstructorLearnerProgressResponse data = progressService.getLearnerProgressByInstructor(
                lessonId, learnerId, currentInstructorId);

        return ResponseEntity.ok(ApiResponse.<InstructorLearnerProgressResponse>builder()
                .message("Get instructor learner progress successfully")
                .data(data)
                .build());
    }

    @GetMapping("/instructor/learners/{learnerId}/progress/summary")
    public ResponseEntity<LearnerProgressSummaryResponse> getLearnerProgressSummary(@PathVariable("learnerId") Long learnerId) {
        LearnerProgressSummaryResponse summary = instructorService.getLearnerProgressSummary(learnerId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/instructor/practice-attempts")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getPracticeAttempts(
            @AuthenticationPrincipal(expression = "user") User currentUser,
            @RequestParam(required = false) Long learnerId,
            @RequestParam(required = false) Long lessonId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate toDate,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String groupBy) {

        InstructorPracticeAttemptRequest filterRequest = InstructorPracticeAttemptRequest.builder()
                .learnerId(learnerId)
                .lessonId(lessonId)
                .fromDate(fromDate)
                .toDate(toDate)
                .page(page)
                .size(size)
                .groupBy(groupBy)
                .build();

        if (groupBy != null && !groupBy.isBlank()) {
            List<PracticeAttemptGroupedResponse> groupedResponses = instructorService.getGroupedPracticeAttemptDetail(currentUser.getId(), filterRequest);
            return ResponseEntity.ok(ApiResponse.<List<PracticeAttemptGroupedResponse>>builder()
                    .message("Get grouped attempts successfully")
                    .data(groupedResponses)
                    .build());
        }

        Page<PracticeAttemptDetailResponse> detailResponsePage = instructorService.getFilteredPracticeAttempts(currentUser.getId(), filterRequest);
        PageResponse<PracticeAttemptDetailResponse> pageResponse = new PageResponse<>(
                detailResponsePage.getContent(),
                detailResponsePage.getNumber(),
                detailResponsePage.getSize(),
                detailResponsePage.getTotalElements(),
                detailResponsePage.getTotalPages(),
                detailResponsePage.isLast()
        );
        return ResponseEntity.ok(ApiResponse.<PageResponse<PracticeAttemptDetailResponse>>builder()
                .message("Get practice attempts successfully")
                .data(pageResponse)
                .build());
    }
}

