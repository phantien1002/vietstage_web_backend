package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MinigameAttemptResponse {
    private Long id;
    private Long minigameId;
    private Long learnerId;
    private Integer score;
    private Integer starsEarned;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer pointsEarned;
    private Integer totalStars;
    private Integer spendableStars;
    private Integer totalPoints;
}
