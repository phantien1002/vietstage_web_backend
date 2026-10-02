package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LearnerCourseProgressResponse {
    private List<LearnerLessonProgressDTO> lessons;
    private List<LearnerLevelProgressDTO> levels;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LearnerLessonProgressDTO {
        private Long lessonId;
        private Boolean isUnlocked;
        @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"})
        private String learningStatus; // NOT_STARTED, IN_PROGRESS, COMPLETED
        private Boolean completed;
        private java.util.Date completedAt;
        private Integer lessonStars;
        private java.math.BigDecimal highestScore;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LearnerLevelProgressDTO {
        private Long levelId;
        private Boolean isUnlocked;
        @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"})
        private String learningStatus;
        private Integer earnedStars;
        private Integer totalStars;
    }
}
