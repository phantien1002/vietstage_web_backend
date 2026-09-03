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
public class ActivityHistoryDetailResponse {
    private String eventId;
    private String type;
    private String lessonTitle;
    private String title;
    private String question;
    private String selectedAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private String challengeType;
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal pitchScore;
    private BigDecimal rhythmScore;
    private BigDecimal dynamicsScore;
    private BigDecimal tonalQualityScore;
    private BigDecimal breathScore;
    private Integer starsEarned;
    private Integer pointsEarned;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status;
}
