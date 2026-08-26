package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseCosmeticResponse {
    private boolean success;
    private String message;
    private PurchaseData data;

    @Data
    @Builder
    public static class PurchaseData {
        private Long cosmeticId;
        private Integer remainingStars;
        private boolean isEquipped;
    }
}
