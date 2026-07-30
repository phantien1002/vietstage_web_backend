package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardEntryResponse {
    private Integer rank;

    @JsonProperty("learner_name")
    private String learnerName;

    @JsonProperty("total_points")
    private Integer totalPoints;

    @JsonProperty("current_streak")
    private Integer currentStreak;
}
