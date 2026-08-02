package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.MinigameAttemptRequest;
import com.example.vietstage_web_be.dto.request.MinigameChallengeRequest;
import com.example.vietstage_web_be.dto.response.MinigameAttemptResponse;
import com.example.vietstage_web_be.dto.response.MinigameChallengeResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IMinigameService;
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
@Tag(name = "Mini Games", description = "Các API quản lý mini games")
public class MinigameController {

    private final IMinigameService minigameService;

    @GetMapping("/lessons/{id}/minigames")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<MinigameChallengeResponse>>> getMinigamesByLesson(
            @PathVariable Long id) {
        List<MinigameChallengeResponse> response = minigameService.getMinigamesByLesson(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/lessons/{id}/minigames")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<MinigameChallengeResponse>> createMinigame(
            @PathVariable Long id,
            @Valid @RequestBody MinigameChallengeRequest request) {
        MinigameChallengeResponse response = minigameService.createMinigame(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PutMapping("/minigames/{id}")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<BaseResponse<MinigameChallengeResponse>> updateMinigame(
            @PathVariable Long id,
            @Valid @RequestBody MinigameChallengeRequest request) {
        MinigameChallengeResponse response = minigameService.updateMinigame(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/minigames/{id}")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteMinigame(@PathVariable Long id) {
        minigameService.deleteMinigame(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/minigames/{id}/attempts")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<MinigameAttemptResponse>> submitAttempt(
            @PathVariable Long id,
            @Valid @RequestBody MinigameAttemptRequest request,
            @AuthenticationPrincipal(expression = "user") User learner) {
        MinigameAttemptResponse response = minigameService.submitAttempt(id, request, learner);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/minigames/{id}/attempts")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<Page<MinigameAttemptResponse>>> getAttempts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal(expression = "user") User learner) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MinigameAttemptResponse> response = minigameService.getAttempts(id, pageable, learner);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}