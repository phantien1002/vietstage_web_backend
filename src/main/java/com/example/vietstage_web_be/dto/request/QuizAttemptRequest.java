package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuizAttemptRequest {
    @NotBlank(message = "Selected answer is required")
    private String selectedAnswer;
    
    @NotBlank(message = "Client attempt id is required")
    private String clientAttemptId;
}
