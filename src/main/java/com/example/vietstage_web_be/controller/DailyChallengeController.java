package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.DailyChallengeRequest;
import com.example.vietstage_web_be.dto.response.CompletionResponse;
import com.example.vietstage_web_be.dto.response.DailyChallengeLearnerResponse;
import com.example.vietstage_web_be.dto.response.DailyChallengeResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IDailyChallengeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-challenges")
@RequiredArgsConstructor
@Tag(name = "Daily Challenges", description = "Các API quản lý thử thách hằng ngày")
public class DailyChallengeController {

    private final IDailyChallengeService dailyChallengeService;

    @GetMapping
    @PreAuthorize("hasAuthority(''LEARNER'')")
    public ResponseEntity<BaseResponse<List<DailyChallengeLearnerResponse>>> getChallenges(
            @AuthenticationPrincipal(expression = "user") User learner,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(BaseResponse.success(dailyChallengeService.getChallenges(date, learner)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(''ADMIN'')")
    public ResponseEntity<BaseResponse<DailyChallengeResponse>> createChallenge(
            @Valid @RequestBody DailyChallengeRequest request) {
        DailyChallengeResponse response = dailyChallengeService.createChallenge(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PostMapping("/{id}/completions")
    @PreAuthorize("hasAuthority(''LEARNER'')")
    public ResponseEntity<BaseResponse<CompletionResponse>> completeChallenge(
            @AuthenticationPrincipal(expression = "user") User learner,
            @PathVariable Long id) {
        CompletionResponse response = dailyChallengeService.completeChallenge(id, learner);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(''ADMIN'')")
    public ResponseEntity<BaseResponse<DailyChallengeResponse>> updateChallenge(
            @PathVariable Long id,
            @Valid @RequestBody DailyChallengeRequest request) {
        DailyChallengeResponse response = dailyChallengeService.updateChallenge(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(''ADMIN'')")
    public ResponseEntity<Void> deleteChallenge(@PathVariable Long id) {
        dailyChallengeService.deleteChallenge(id);
        return ResponseEntity.noContent().build();
    }
}
