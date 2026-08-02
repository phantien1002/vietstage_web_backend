package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.FeedbackRequest;
import com.example.vietstage_web_be.dto.response.FeedbackResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IFeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Các API quản lý nhận xét của giảng viên")
public class FeedbackController {

    private final IFeedbackService feedbackService;

    @PostMapping("/practice/attempts/{id}/feedback")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<BaseResponse<FeedbackResponse>> submitFeedback(
            @AuthenticationPrincipal(expression = "user") User instructor,
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest request) {
        FeedbackResponse response = feedbackService.submitFeedback(instructor, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/practice/attempts/{id}/feedback")
    @PreAuthorize("hasAnyAuthority('LEARNER', 'INSTRUCTOR')")
    public ResponseEntity<BaseResponse<List<FeedbackResponse>>> getFeedbackForAttempt(
            @PathVariable Long id) {
        List<FeedbackResponse> response = feedbackService.getFeedbackForAttempt(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}