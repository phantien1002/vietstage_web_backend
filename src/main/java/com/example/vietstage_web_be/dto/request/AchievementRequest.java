package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AchievementRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Icon URL is required")
    private String iconUrl;

    @NotBlank(message = "Condition JSON is required")
    private String conditionJson;
}
