package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseCosmeticResponse {
    private Long cosmeticId;
    private Integer totalStars;
    private Integer spendableStars;
    private Boolean isEquipped;
}
