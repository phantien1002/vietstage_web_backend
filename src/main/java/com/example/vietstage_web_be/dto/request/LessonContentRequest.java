package com.example.vietstage_web_be.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonContentRequest {
    
    @NotBlank(message = "Nội dung không được để trống")
    @JsonProperty("content_text")
    private String contentText;

    @NotNull
    @JsonProperty("order_index")
    private Integer orderIndex;
}