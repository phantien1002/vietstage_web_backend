package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorLearnerProgressResponse {
    private Long lessonId;
    private Long learnerId;
    private Boolean isUnlocked;
    @Schema(allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"})
    private String learningStatus;
    private Integer stars;
    private Boolean completed;
    private Integer totalPracticeAttempts;
    private Double bestPracticeScore;
    private Integer totalQuizAttempts;
}
