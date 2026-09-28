package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LearnerCosmeticResponse {
    private Long id;
    private String name;
    private String itemType;
    private Integer starPrice;
    private String assetUrl;
    @com.fasterxml.jackson.annotation.JsonProperty("isEquipped")
    private Boolean isEquipped;
}
