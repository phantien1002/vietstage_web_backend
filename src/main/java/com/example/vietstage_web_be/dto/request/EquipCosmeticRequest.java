package com.example.vietstage_web_be.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipCosmeticRequest {
    @NotNull(message = "is_equipped is required")
    @JsonProperty("is_equipped")
    private Boolean isEquipped;
}
