package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LearnerSummaryResponse {
    private Long learnerId;
    private String learnerName;
    private Long totalLessonsCompleted;
    private Long totalPracticeAttempts;
    private BigDecimal averagePracticeScore;
    private Integer currentStreakDays;
    private Long totalPracticeDurationMinutes;
}
