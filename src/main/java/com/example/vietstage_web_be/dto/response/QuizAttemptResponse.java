package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class QuizAttemptResponse {
    private Long id;
    private Long quizId;
    private Long learnerId;
    private String selectedAnswer;
    private Boolean isCorrect;
    private BigDecimal score;
    private LocalDateTime attemptedAt;
    
    private String correctAnswer; // added for review functionality
    
    // Add pointsEarned if required by API doc "201 -> {is_correct, score, points_earned}"
    private Integer pointsEarned;
}
