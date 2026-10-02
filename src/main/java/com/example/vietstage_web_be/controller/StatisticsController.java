package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.response.ActivityStatisticsResponse;
import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instructor/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Các API thống kê kết quả học tập cho Instructor")
public class StatisticsController {

    private final IStatisticsService statisticsService;

    @GetMapping("/quizzes/{quizId}")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Thống kê Quiz", description = "Trả về số lượng học viên, số lượt làm, điểm trung bình và tỷ lệ đúng cho một Quiz cụ thể")
    public ResponseEntity<BaseResponse<ActivityStatisticsResponse>> getQuizStatistics(
            @PathVariable Long quizId,
            @AuthenticationPrincipal(expression = "user") User actor) {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getQuizStatistics(quizId, actor)));
    }

    @GetMapping("/minigames/{minigameId}")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Thống kê Minigame", description = "Trả về số lượng học viên, số lượt làm, điểm trung bình và sao trung bình cho một Minigame cụ thể")
    public ResponseEntity<BaseResponse<ActivityStatisticsResponse>> getMinigameStatistics(
            @PathVariable Long minigameId,
            @AuthenticationPrincipal(expression = "user") User actor) {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getMinigameStatistics(minigameId, actor)));
    }

    @GetMapping("/lessons/{lessonId}/quizzes")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Thống kê Quiz theo Bài học", description = "Thống kê tổng hợp các Quiz trong một Bài học")
    public ResponseEntity<BaseResponse<ActivityStatisticsResponse>> getLessonQuizStatistics(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal(expression = "user") User actor) {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getLessonQuizStatistics(lessonId, actor)));
    }

    @GetMapping("/lessons/{lessonId}/minigames")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Thống kê Minigame theo Bài học", description = "Thống kê tổng hợp các Minigame trong một Bài học")
    public ResponseEntity<BaseResponse<ActivityStatisticsResponse>> getLessonMinigameStatistics(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal(expression = "user") User actor) {
        return ResponseEntity.ok(BaseResponse.success(statisticsService.getLessonMinigameStatistics(lessonId, actor)));
    }
}
