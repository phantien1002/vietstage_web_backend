package com.example.vietstage_web_be.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EndSessionRequest {
    @NotNull(message = "Thời gian kết thúc không được để trống")
    @JsonProperty("ended_at")
    private LocalDateTime endedAt;
}