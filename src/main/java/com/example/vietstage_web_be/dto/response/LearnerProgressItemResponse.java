package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerProgressItemResponse {
    private Long lessonId;
    private String lessonCode;
    private String instrumentCode;
    private String levelCode;
    private Integer orderIndex;
    private String title;
    private Integer stars;
    private java.math.BigDecimal highestScore;
    private Boolean completed;
    private java.util.Date completedAt;
}
