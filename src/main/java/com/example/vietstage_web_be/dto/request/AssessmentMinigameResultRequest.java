package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssessmentMinigameResultRequest {
    @NotNull private Long challengeId;
    @NotNull private Integer score;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
