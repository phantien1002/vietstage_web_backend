package com.example.vietstage_web_be.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LessonAssetRequest {
    @JsonProperty("tempo_bpm")
    private Integer tempoBpm;

    @JsonProperty("duration_sec")
    private Integer durationSec;
}