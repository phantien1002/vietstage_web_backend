package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonAssetResponse {
    private Long id;
    private String type;
    private String url;
    
    @JsonProperty("tempo_bpm")
    private Integer tempoBpm;
    
    @JsonProperty("duration_sec")
    private Integer durationSec;
}