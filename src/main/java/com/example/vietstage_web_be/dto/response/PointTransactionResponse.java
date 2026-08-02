package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PointTransactionResponse {
    private Long id;

    @JsonProperty("source_type")
    private String sourceType;

    private Integer points;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
