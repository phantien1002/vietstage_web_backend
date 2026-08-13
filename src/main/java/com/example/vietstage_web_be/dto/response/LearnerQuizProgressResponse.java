package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerQuizProgressResponse {
    private Long id;
    private Long lessonId;
    private String lessonCode;
    private Long instrumentId;
    private String instrumentCode;
    private Long skillLevelId;
    private String levelCode;
    private Short levelOrderIndex;
    private String question;
    private String options;
    private String correctAnswer;
    private Integer orderIndex;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    private Boolean attempted;
    private String selectedAnswer;
    private Boolean correct;
    private BigDecimal score;
    private LocalDateTime attemptedAt;
}
