package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizResponse {
    private Long id;
    private String question;
    private String options; // JSON string array
    
    // Only populated for INSTRUCTOR/ADMIN, null for LEARNER
    private String correctAnswer;
    
    private Integer orderIndex;
}
