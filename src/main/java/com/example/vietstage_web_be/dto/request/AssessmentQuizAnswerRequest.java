package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssessmentQuizAnswerRequest {
    @NotNull private Long quizId;
    @NotBlank private String selectedAnswer;
}
