package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.response.LeaderboardEntryResponse;
import com.example.vietstage_web_be.dto.response.MyLeaderboardResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.PointTransactionResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.ILeaderboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.example.vietstage_web_be.repository.AppConfigRepository;
import com.example.vietstage_web_be.entity.AppConfig;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Leaderboards", description = "Các API quản lý bảng xếp hạng và điểm")
public class LeaderboardController {

    private final ILeaderboardService leaderboardService;
    private final AppConfigRepository appConfigRepository;

    @GetMapping("/leaderboards")
    public ResponseEntity<BaseResponse<List<LeaderboardEntryResponse>>> getTopLeaderboard(
            @RequestParam(defaultValue = "100") int top) {
        
        boolean isEnabled = appConfigRepository.findByConfigKey("feature.leaderboard.enabled")
                .map(AppConfig::getConfigValue)
                .map(Boolean::parseBoolean)
                .orElse(true);
        if (!isEnabled) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tính năng Leaderboard hiện đang bị tắt");
        }

        List<LeaderboardEntryResponse> response = leaderboardService.getTopLeaderboard(top);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/leaderboards/me")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<MyLeaderboardResponse>> getMyLeaderboard(
            @AuthenticationPrincipal(expression = "user") User learner) {
        
        boolean isEnabled = appConfigRepository.findByConfigKey("feature.leaderboard.enabled")
                .map(AppConfig::getConfigValue)
                .map(Boolean::parseBoolean)
                .orElse(true);
        if (!isEnabled) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tính năng Leaderboard hiện đang bị tắt");
        }

        MyLeaderboardResponse response = leaderboardService.getMyLeaderboard(learner);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/users/{id}/point-transactions")
    @PreAuthorize("hasAnyAuthority('LEARNER', 'ADMIN')")
    public ResponseEntity<BaseResponse<PageResponse<PointTransactionResponse>>> getPointTransactions(
            @PathVariable Long id,
            @RequestParam(required = false) String source_type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<PointTransactionResponse> response = leaderboardService.getPointTransactions(id, source_type, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}