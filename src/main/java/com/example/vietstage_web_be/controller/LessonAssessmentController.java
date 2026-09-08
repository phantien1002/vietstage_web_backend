package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.LessonAssessmentRequest;
import com.example.vietstage_web_be.dto.response.LessonAssessmentResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.ILessonAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/lessons") @RequiredArgsConstructor
public class LessonAssessmentController {
    private final ILessonAssessmentService service;
    @PostMapping("/{lessonId}/assessment-sessions") @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<LessonAssessmentResponse>> submit(@PathVariable Long lessonId, @Valid @RequestBody LessonAssessmentRequest request, @AuthenticationPrincipal(expression = "user") User learner) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(service.submit(lessonId, request, learner)));
    }
    @GetMapping("/assessment-sessions/{sessionId}") @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<LessonAssessmentResponse>> detail(@PathVariable Long sessionId, @AuthenticationPrincipal(expression = "user") User learner) {
        return ResponseEntity.ok(BaseResponse.success(service.getDetail(sessionId, learner)));
    }
}
