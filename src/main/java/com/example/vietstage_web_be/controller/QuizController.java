package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.LearnerQuizRequest;
import com.example.vietstage_web_be.dto.request.QuizAttemptRequest;
import com.example.vietstage_web_be.dto.request.QuizRequest;
import com.example.vietstage_web_be.dto.response.LearnerQuizProgressResponse;
import com.example.vietstage_web_be.dto.response.QuizAttemptResponse;
import com.example.vietstage_web_be.dto.response.QuizResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IQuizService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Quizzes", description = "Các API quản lý trắc nghiệm")
public class QuizController {

    private final IQuizService quizService;

    @GetMapping("/lessons/{id}/quizzes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<QuizResponse>>> getQuizzesByLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "user") User currentUser) {
        
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
            @AuthenticationPrincipal(expression = "user") User learner) {
            
        QuizAttemptResponse response = quizService.submitAttempt(id, request, learner);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/quizzes/{id}/attempts")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<Page<QuizAttemptResponse>>> getAttempts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal(expression = "user") User learner) {
            
        Pageable pageable = PageRequest.of(page, size);
        Page<QuizAttemptResponse> response = quizService.getAttempts(id, pageable, learner);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/instructor/quizzes/learner-level")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<BaseResponse<LearnerQuizProgressResponse>> createQuizByLearnerLevel(@Valid @RequestBody LearnerQuizRequest request){
        LearnerQuizProgressResponse response = quizService.createQuizByLearnerLever(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PutMapping("/instructor/quizzes/{quizId}/learner-level")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<BaseResponse<LearnerQuizProgressResponse>> updateQuizByLearnerLevel(@PathVariable Long quizId,
                                                                                              @Valid @RequestBody LearnerQuizRequest request){
        LearnerQuizProgressResponse response = quizService.updateQuizByLearnerLevel(quizId, request);

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/instructor/quizzes/learner-level")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<BaseResponse<LearnerQuizProgressResponse>> getQuizByLearnerLevel(@RequestBody Long learnerId,
                                                                                           @RequestBody Long instrumentId){
        List<LearnerQuizProgressResponse> responses = quizService.getQuizzesByLearnerLevel(learnerId, instrumentId);

        return ResponseEntity.ok(BaseResponse.success((LearnerQuizProgressResponse) responses));
    }

    @GetMapping("/learner/quizzes")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<List<LearnerQuizProgressResponse>>> getQuizzes(@RequestBody Long instrumentId,
                                                                                      @RequestBody(required = false) Long lessonId,
                                                                                      @AuthenticationPrincipal(expression = "user") User learner) {
        List<LearnerQuizProgressResponse> responses = quizService.getQuizzes(learner.getId(), instrumentId, lessonId);

        return ResponseEntity.ok(BaseResponse.success(responses));
    }

}