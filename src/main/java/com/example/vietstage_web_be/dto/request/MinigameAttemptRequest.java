package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MinigameAttemptRequest {
    @NotNull(message = "Score is required")
    private Integer score;

    @NotBlank(message = "Client attempt id is required")
    private String clientAttemptId;

    @NotNull(message = "Started at is required")
    private LocalDateTime startedAt;

    @NotNull(message = "Completed at is required")
    private LocalDateTime completedAt;
    
    @io.swagger.v3.oas.annotations.media.Schema(
        description = "Dữ liệu JSON chi tiết của lượt chơi để server xác minh. " +
        "Với RHYTHM_MATCH: { \"totalTargets\": 10, \"hits\": 8, \"accuracy\": 80.0 }. " +
        "Với MELODY_COMPLETE: { \"selectedNotes\": [\"C\", \"D\"], \"correctNotes\": [\"C\", \"D\"], \"isCorrect\": true, \"correctRounds\": 1 }",
        example = "{\"totalTargets\": 10, \"hits\": 8, \"accuracy\": 80.0}"
    )
    private String playData;
}
