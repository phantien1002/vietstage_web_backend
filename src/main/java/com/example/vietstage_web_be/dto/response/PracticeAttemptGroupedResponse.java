package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeAttemptGroupedResponse {
    private String timeGroup;
    private Long totalAttempts;
    private BigDecimal averageTotalScore;
    private Long totalPointsEarned;
    private Long totalStarsEarned;
}
