package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonContentResponse {
    private Long id;

    @JsonProperty("content_text")
    private String contentText;

    @JsonProperty("order_index")
    private Integer orderIndex;
}