package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.QuizAttemptRequest;
import com.example.vietstage_web_be.dto.request.QuizRequest;
import com.example.vietstage_web_be.dto.response.QuizAttemptResponse;
import com.example.vietstage_web_be.dto.response.QuizResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final IQuizService quizService;

    @GetMapping("/lessons/{id}/quizzes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<QuizResponse>>> getQuizzesByLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        List<QuizResponse> response = quizService.getQuizzesByLesson(id, currentUser);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/lessons/{id}/quizzes")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<QuizResponse>> createQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequest request) {
            
        QuizResponse response = quizService.createQuiz(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PutMapping("/quizzes/{id}")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<QuizResponse>> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequest request) {
            
        QuizResponse response = quizService.updateQuiz(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/quizzes/{id}")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quizzes/{id}/attempts")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<QuizAttemptResponse>> submitAttempt(
            @PathVariable Long id,
            @Valid @RequestBody QuizAttemptRequest request,
            @AuthenticationPrincipal User learner) {
            
        QuizAttemptResponse response = quizService.submitAttempt(id, request, learner);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/quizzes/{id}/attempts")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<Page<QuizAttemptResponse>>> getAttempts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User learner) {
            
        Pageable pageable = PageRequest.of(page, size);
        Page<QuizAttemptResponse> response = quizService.getAttempts(id, pageable, learner);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}