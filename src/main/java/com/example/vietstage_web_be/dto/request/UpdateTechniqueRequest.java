package com.example.vietstage_web_be.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTechniqueRequest {
    @NotBlank(message = "Technique name cannot be blank")
    private String name;

    private String description;

    @JsonProperty("guide_url")
    private String guideUrl;
}
