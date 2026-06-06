package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstrumentRequest {
    @NotBlank(message = "Instrument name cannot be blank")
    private String name;

    private String description;
}
