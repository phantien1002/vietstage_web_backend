package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DailyChallengeResponse {
    private Long id;
    private String title;
    private String description;
    
    @JsonProperty("instrument_id")
    private Long instrumentId;
    
    @JsonProperty("reward_points")
    private Integer rewardPoints;
    
    @JsonProperty("challenge_date")
    private LocalDate challengeDate;
}