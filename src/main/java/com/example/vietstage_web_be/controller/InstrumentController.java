package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.QuizRequest;
import com.example.vietstage_web_be.dto.response.BaseResponse;
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

    @PostMapping("/{instrumentId}/quizzes")
    public ResponseEntity<BaseResponse<QuizResponse>> createQuizByInstrument(
            @AuthenticationPrincipal User actor,
            @PathVariable Long instrumentId,
            @RequestBody QuizRequest request) {
        
        QuizResponse response = quizService.createQuizByInstrument(actor, instrumentId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/{instrumentId}/quizzes")
    public ResponseEntity<BaseResponse<List<QuizResponse>>> getQuizzesByInstrument(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long instrumentId) {
        
        List<QuizResponse> responses = quizService.getQuizzesByInstrument(instrumentId, currentUser);
        return ResponseEntity.ok(BaseResponse.success(responses));
    }
}
