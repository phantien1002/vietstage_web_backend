package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonVisibilityRequest {
    @NotNull(message = "isVisible must not be null")
    private Boolean isVisible;
    private String hiddenReason;
}
