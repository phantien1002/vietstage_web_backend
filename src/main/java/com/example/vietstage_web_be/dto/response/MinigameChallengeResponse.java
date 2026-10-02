package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MinigameChallengeResponse {
    private Long id;
    private String title;
    @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"RHYTHM_MATCH", "MELODY_COMPLETE"})
    private String challengeType;
    private String difficulty;
    private Integer maxScore;
    private Integer orderIndex;
    
    // We omit referenceAssetId and just return contentJson as specified in my plan
    private String contentJson;
    
    @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"ACTIVE", "INACTIVE", "ARCHIVED"})
    private String status;
}
