package com.example.vietstage_web_be.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonTechniqueRequest {
    @NotNull(message = "technique_id không được để trống")
    @JsonProperty("technique_id")
    private Long techniqueId;
}