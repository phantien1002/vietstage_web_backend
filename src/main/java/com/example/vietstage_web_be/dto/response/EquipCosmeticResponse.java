package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EquipCosmeticResponse {
    @JsonProperty("cosmetic_id")
    private Long cosmeticId;
    
    @JsonProperty("is_equipped")
    private Boolean isEquipped;
}
