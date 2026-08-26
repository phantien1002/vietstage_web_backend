package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MyCosmeticsResponse {
    private Integer totalStars;
    private Integer spendableStars;
    private List<LearnerCosmeticResponse> owned;
    private List<CosmeticItemResponse> locked;
}
