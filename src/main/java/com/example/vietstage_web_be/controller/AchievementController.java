package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.AchievementRequest;
import com.example.vietstage_web_be.dto.response.AchievementResponse;
import com.example.vietstage_web_be.dto.response.LearnerAchievementsResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IAchievementService;
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
@Tag(name = "Achievements", description = "Các API quản lý thành tựu")
public class AchievementController {

    private final IAchievementService achievementService;

    @GetMapping("/achievements")
    public ResponseEntity<BaseResponse<List<AchievementResponse>>> getAllAchievements() {
        List<AchievementResponse> response = achievementService.getAllAchievements();
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/achievements")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<AchievementResponse>> createAchievement(
            @Valid @RequestBody AchievementRequest request) {
        AchievementResponse response = achievementService.createAchievement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/users/me/achievements")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<LearnerAchievementsResponse>> getMyAchievements(
            @AuthenticationPrincipal User learner) {
        LearnerAchievementsResponse response = achievementService.getMyAchievements(learner);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping("/achievements/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<AchievementResponse>> updateAchievement(
            @PathVariable Long id,
            @Valid @RequestBody AchievementRequest request) {
        AchievementResponse response = achievementService.updateAchievement(id, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/users/{learnerId}/achievements/{achievementId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> revokeAchievement(
            @PathVariable Long learnerId,
            @PathVariable Long achievementId) {
        achievementService.revokeAchievement(learnerId, achievementId);
        return ResponseEntity.noContent().build();
    }
}