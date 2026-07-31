package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DailyChallengeLearnerResponse {
    private Long id;
    private String title;
    private String description;
    
    @JsonProperty("instrument_id")
    private Long instrumentId;
    
    @JsonProperty("reward_points")
    private Integer rewardPoints;
    
    @JsonProperty("challenge_date")
    private LocalDate challengeDate;
    
    @JsonProperty("is_completed")
    private boolean isCompleted;
    
    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
    
    @JsonProperty("points_earned")
    private Integer pointsEarned;
}