package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonAccessResponse {
    private Boolean isUnlocked;
    private String learningStatus; // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
}
