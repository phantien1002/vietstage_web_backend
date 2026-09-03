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
public class ActivityHistoryItemResponse {
    private String eventId;
    private String type;
    private Long lessonId;
    private String lessonTitle;
    private String title;
    private BigDecimal score;
    private BigDecimal maxScore;
    private Integer starsEarned;
    private Integer pointsEarned;
    private LocalDateTime completedAt;
    private String status;
}
