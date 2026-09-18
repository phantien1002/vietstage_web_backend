package com.example.vietstage_web_be.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonContentRequest {
    
    @JsonProperty("content_text")
    private String contentText;

    @NotNull
    @JsonProperty("order_index")
    private Integer orderIndex;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("payload_json")
    private String payloadJson;

    @JsonProperty("asset_id")
    private Long assetId;
}