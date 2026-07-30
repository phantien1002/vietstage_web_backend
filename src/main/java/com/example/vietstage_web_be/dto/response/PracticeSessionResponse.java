package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PracticeSessionResponse {
    private Long id;

    @JsonProperty("started_at")
    private LocalDateTime startedAt;

    @JsonProperty("ended_at")
    private LocalDateTime endedAt;

    @JsonProperty("duration_minutes")
    private Integer durationMinutes;
}