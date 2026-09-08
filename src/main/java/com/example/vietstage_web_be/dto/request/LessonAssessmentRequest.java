package com.example.vietstage_web_be.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LessonAssessmentRequest {
    @NotBlank private String clientSessionId;
    @NotNull private LocalDateTime startedAt;
    @NotNull @Valid private List<AssessmentQuizAnswerRequest> quizAnswers;
    @NotNull @Valid private List<AssessmentMinigameResultRequest> minigameResults;
}
