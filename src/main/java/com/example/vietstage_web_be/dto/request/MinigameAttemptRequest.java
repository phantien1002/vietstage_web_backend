package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MinigameAttemptRequest {
    @NotNull(message = "Score is required")
    private Integer score;

    @NotNull(message = "Stars earned is required")
    private Integer starsEarned;

    @NotNull(message = "Started at is required")
    private LocalDateTime startedAt;

    @NotNull(message = "Completed at is required")
    private LocalDateTime completedAt;
    
    // Optional because RHYTHM_MATCH might not use it or client scores it
    private java.util.Map<String, String> answers;
}
