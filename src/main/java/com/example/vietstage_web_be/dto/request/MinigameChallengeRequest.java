package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MinigameChallengeRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Challenge type is required")
    @jakarta.validation.constraints.Pattern(regexp = "^(RHYTHM_MATCH|MELODY_COMPLETE)$", message = "Challenge type must be RHYTHM_MATCH or MELODY_COMPLETE")
    @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"RHYTHM_MATCH", "MELODY_COMPLETE"})
    private String challengeType;

    // Optional depending on type
    private String contentJson;
    
    // Optional depending on type
    private Long referenceAssetId;

    private String difficulty;

    @NotNull(message = "Max score is required")
    private Integer maxScore;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;
}
