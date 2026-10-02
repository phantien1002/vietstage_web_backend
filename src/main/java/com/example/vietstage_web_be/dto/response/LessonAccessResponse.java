package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonAccessResponse {
    private Boolean isUnlocked;
    @Schema(allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"})
    private String learningStatus; // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
}
