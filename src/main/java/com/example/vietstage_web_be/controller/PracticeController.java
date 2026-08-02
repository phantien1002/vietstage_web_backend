package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.BulkPracticeAttemptRequest;
import com.example.vietstage_web_be.dto.request.EndSessionRequest;
import com.example.vietstage_web_be.dto.request.PracticeAttemptRequest;
import com.example.vietstage_web_be.dto.response.BulkPracticeAttemptResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.PracticeAttemptResponse;
import com.example.vietstage_web_be.dto.response.PracticeSessionResponse;
import com.example.vietstage_web_be.entity.PracticeAttempt;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IPracticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Practice", description = "Các API quản lý phiên tập và kết quả AI")
public class PracticeController {

    private final IPracticeService practiceService;

    @PostMapping("/practice/sessions")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<PracticeSessionResponse>> startSession(@AuthenticationPrincipal User learner) {
        PracticeSessionResponse response = practiceService.startSession(learner);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PutMapping("/practice/sessions/{id}")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<PracticeSessionResponse>> endSession(
            @AuthenticationPrincipal User learner,
            @PathVariable Long id,
            @Valid @RequestBody EndSessionRequest request) {
        PracticeSessionResponse response = practiceService.endSession(learner, id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/practice/sessions")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<PageResponse<PracticeSessionResponse>>> getHistorySessions(
            @AuthenticationPrincipal User learner,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startedAt").descending());
        PageResponse<PracticeSessionResponse> response = practiceService.getHistorySessions(learner, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/practice/attempts")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<PracticeAttemptResponse>> submitAttempt(
            @AuthenticationPrincipal User learner,
            @Valid @RequestBody PracticeAttemptRequest request) {
        PracticeAttemptResponse response = practiceService.submitAttempt(learner, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/practice/attempts")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<PageResponse<PracticeAttempt>>> getMyAttempts(
            @AuthenticationPrincipal User learner,
            @RequestParam(required = false) Long exercise_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<PracticeAttempt> response = practiceService.getMyAttempts(learner, exercise_id, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/practice/attempts/{id}")
    @PreAuthorize("hasAnyAuthority('LEARNER', 'INSTRUCTOR')")
    public ResponseEntity<BaseResponse<PracticeAttempt>> getAttemptDetails(@PathVariable Long id) {
        PracticeAttempt response = practiceService.getAttemptDetails(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/lessons/{id}/attempts")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<BaseResponse<PageResponse<PracticeAttempt>>> getLearnerAttemptsForLesson(
            @AuthenticationPrincipal User instructor,
            @PathVariable Long id,
            @RequestParam Long learner_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<PracticeAttempt> response = practiceService.getLearnerAttemptsForLesson(instructor, id, learner_id, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/practice/attempts/bulk")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<BulkPracticeAttemptResponse>> submitBulkAttempts(
            @AuthenticationPrincipal User learner,
            @Valid @RequestBody BulkPracticeAttemptRequest request) {
        BulkPracticeAttemptResponse response = practiceService.submitBulkAttempts(learner, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}