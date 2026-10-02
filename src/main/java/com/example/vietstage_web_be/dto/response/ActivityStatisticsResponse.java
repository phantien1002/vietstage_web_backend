package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityStatisticsResponse {
    private Long learnersCount;
    private Long totalAttempts;
    private Double averageScore;
    
    // For Minigame
    private Double averageStars;
    
    // For Quiz
    private Double passRate;
}
