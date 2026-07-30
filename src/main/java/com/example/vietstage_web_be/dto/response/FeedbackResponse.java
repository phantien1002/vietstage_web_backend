package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {
    private Long id;

    @JsonProperty("instructor_name")
    private String instructorName;

    private String comment;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}