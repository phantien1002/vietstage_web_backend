package com.example.vietstage_web_be.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyChallengeRequest {
    
    @NotBlank
    private String title;
    
    private String description;
    
    @NotNull
    @JsonProperty("instrument_id")
    private Long instrumentId;
    
    @NotNull
    @JsonProperty("reward_points")
    private Integer rewardPoints;
    
    @NotNull
    @JsonProperty("challenge_date")
    private LocalDate challengeDate;
}