package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyLeaderboardResponse {
    private Integer rank;

    @JsonProperty("total_points")
    private Integer totalPoints;

    private Double percentile;
}
