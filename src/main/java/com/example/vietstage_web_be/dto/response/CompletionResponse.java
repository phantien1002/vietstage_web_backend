package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CompletionResponse {
    @JsonProperty("challenge_id")
    private Long challengeId;
    
    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
    
    @JsonProperty("points_earned")
    private Integer pointsEarned;
}