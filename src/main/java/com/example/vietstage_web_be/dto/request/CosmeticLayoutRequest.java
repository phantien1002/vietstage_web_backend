package com.example.vietstage_web_be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.Valid;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosmeticLayoutRequest {
    @NotNull(message = "items cannot be null")
    @Valid
    private List<LayoutItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LayoutItem {
        @NotNull(message = "cosmeticId is required")
        private Long cosmeticId;
        
        @NotNull(message = "x is required")
        private Double x;
        
        @NotNull(message = "y is required")
        private Double y;
        
        @NotNull(message = "scale is required")
        @DecimalMin(value = "0.1", message = "Scale must be at least 0.1")
        @DecimalMax(value = "10.0", message = "Scale must be at most 10.0")
        private Double scale;
        
        @NotNull(message = "zIndex is required")
        private Integer zIndex;
    }
}
