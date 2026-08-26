package com.example.vietstage_web_be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosmeticLayoutRequest {
    private List<LayoutItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayoutItem {
        private Long cosmeticId;
        private Double x;
        private Double y;
        private Double scale;
        private Integer zIndex;
    }
}
