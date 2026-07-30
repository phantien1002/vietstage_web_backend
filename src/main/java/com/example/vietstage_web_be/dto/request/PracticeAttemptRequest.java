package com.example.vietstage_web_be.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PracticeAttemptRequest {

    @NotNull
    @JsonProperty("session_id")
    private Long sessionId;

    @NotNull
    @JsonProperty("exercise_id")
    private Long exerciseId;

    @JsonProperty("pitch_score")
    private BigDecimal pitchScore;

    @JsonProperty("rhythm_score")
    private BigDecimal rhythmScore;

    @JsonProperty("dynamics_score")
    private BigDecimal dynamicsScore;

    @JsonProperty("tonal_quality_score")
    private BigDecimal tonalQualityScore;

    @JsonProperty("breath_score")
    private BigDecimal breathScore;

    @JsonProperty("client_uuid")
    private String clientUuid;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}