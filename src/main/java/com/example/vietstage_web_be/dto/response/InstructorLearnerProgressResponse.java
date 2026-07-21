package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorLearnerProgressResponse {
    private Long lessonId;
    private Long learnerId;
    private Integer stars;
    private Boolean completed;
    private Integer totalPracticeAttempts;
    private Double bestPracticeScore;
    private Integer totalQuizAttempts;
}
