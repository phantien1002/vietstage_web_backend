package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCompletionResponse {
    private Long lessonId;
    private Boolean completed;
    private Integer lessonStars;
    private Integer starsEarned;
    private Integer totalStars;
    private Integer spendableStars;
    private Integer totalPoints;
}
