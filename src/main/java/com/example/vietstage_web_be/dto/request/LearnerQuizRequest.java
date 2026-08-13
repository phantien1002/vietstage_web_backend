package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerQuizRequest {
    @NotNull(message = "Learner ID is required")
    private Long learnerId;

    @NotNull(message = "Lesson ID is required")
    private Long lessonId;

    @NotNull(message = "Instrument ID is required")
    private Long instrumentId;

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Options are required")
    private String options;

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;
}
