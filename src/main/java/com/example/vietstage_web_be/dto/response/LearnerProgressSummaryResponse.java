package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerProgressSummaryResponse {
    @JsonProperty("total_stars")
    private Integer totalStars;
    
    @JsonProperty("spendable_stars")
    private Integer spendableStars;
    
    @JsonProperty("completed_lessons")
    private Long completedLessons;
    
    @JsonProperty("current_streak")
    private Integer currentStreak;
    
    @JsonProperty("longest_streak")
    private Integer longestStreak;
    
    @JsonProperty("total_points")
    private Integer totalPoints;
    
    @JsonProperty("adaptive_difficulty")
    private Integer adaptiveDifficulty;
}
