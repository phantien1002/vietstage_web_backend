package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeAttemptDetailResponse {
    private Long attemptId;
    private Long learnerId;
    private String learnerName;
    private Long lessonId;
    private String lessonTitle;
    private Long exerciseId;
    private String exerciseTitle;
    private BigDecimal pitchScore;
    private BigDecimal rhythmScore;
    private BigDecimal dynamicsScore;
    private BigDecimal totalScore;
    private Integer stars;
    private Integer pointsEarned;
    private Boolean isPassed;
    private String syncStatus;
    private Integer durationSeconds;
    private LocalDateTime attemptedAt;
}
