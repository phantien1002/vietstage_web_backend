package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PracticeAttemptResponse {
    private Long id;

    @JsonProperty("total_score")
    private BigDecimal totalScore;

    private Integer stars;

    @JsonProperty("points_earned")
    private Integer pointsEarned;
}