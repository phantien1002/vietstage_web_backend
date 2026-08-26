package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EquipCosmeticResponse {
    @JsonProperty("cosmeticId")
    private Long cosmeticId;
    
    @JsonProperty("isEquipped")
    private Boolean isEquipped;
}
