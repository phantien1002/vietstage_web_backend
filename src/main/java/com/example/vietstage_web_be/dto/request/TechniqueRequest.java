package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TechniqueRequest {
    @NotBlank(message = "Technique name cannot be blank")
    private String name;

    private String description;

    @NotNull(message = "Instrument ID cannot be null")
    private Long instrumentId;
}
