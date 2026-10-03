package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.QuizRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.QuizResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {
    private final IQuizService quizService;
    private final com.example.vietstage_web_be.service.IMinigameService minigameService;

    @PostMapping("/{instrumentId}/quizzes")
    public ResponseEntity<ApiResponse<QuizResponse>> createQuizByInstrument(
            @AuthenticationPrincipal User actor,
            @PathVariable Long instrumentId,
            @RequestBody QuizRequest request) {
        
        QuizResponse response = quizService.createQuizByInstrument(actor, instrumentId, request);
        return ResponseEntity.ok(ApiResponse.<QuizResponse>builder().data(response).build());
    }

    @GetMapping("/{instrumentId}/quizzes")
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzesByInstrument(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long instrumentId) {
        
        List<QuizResponse> responses = quizService.getQuizzesByInstrument(instrumentId, currentUser);
        return ResponseEntity.ok(ApiResponse.<List<QuizResponse>>builder().data(responses).build());
    }

    @PostMapping("/{instrumentId}/minigames")
    public ResponseEntity<ApiResponse<com.example.vietstage_web_be.dto.response.MinigameChallengeResponse>> createMinigameByInstrument(
            @AuthenticationPrincipal User actor,
            @PathVariable Long instrumentId,
            @RequestBody com.example.vietstage_web_be.dto.request.MinigameChallengeRequest request) {
        
        com.example.vietstage_web_be.dto.response.MinigameChallengeResponse response = minigameService.createMinigameByInstrument(actor, instrumentId, request);
        return ResponseEntity.ok(ApiResponse.<com.example.vietstage_web_be.dto.response.MinigameChallengeResponse>builder().data(response).build());
    }

    @GetMapping("/{instrumentId}/minigames")
    public ResponseEntity<ApiResponse<List<com.example.vietstage_web_be.dto.response.MinigameChallengeResponse>>> getMinigamesByInstrument(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long instrumentId) {
        
        List<com.example.vietstage_web_be.dto.response.MinigameChallengeResponse> responses = minigameService.getMinigamesByInstrument(instrumentId, currentUser);
        return ResponseEntity.ok(ApiResponse.<List<com.example.vietstage_web_be.dto.response.MinigameChallengeResponse>>builder().data(responses).build());
    }
}
