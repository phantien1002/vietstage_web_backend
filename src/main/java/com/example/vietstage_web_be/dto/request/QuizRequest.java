package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Options are required")
    private String options; // JSON string array

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;
}
