package com.example.vietstage_web_be.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LessonAssessmentResponse {
    private Long id;
    private Long lessonId;
    private String clientSessionId;
    private String status;
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal accuracy;
    private Integer starsEarned;
    private Integer pointsEarned;
    private Boolean completed;
    private Integer lessonStars;
    private Integer totalStars;
    private Integer totalPoints;
    private LocalDateTime completedAt;
    private List<QuizAnswer> quizAnswers;
    private List<MinigameResult> minigameResults;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizAnswer { private Long quizId; private String selectedAnswer; private Boolean correct; private BigDecimal score; private BigDecimal maxScore; }
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MinigameResult { private Long challengeId; private Integer score; private Integer maxScore; }
}
