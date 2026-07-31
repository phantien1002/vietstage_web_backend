package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppConfigResponse {
    
    private String key;
    private String value;
    private String description;
    
    @JsonProperty("config_group")
    private String configGroup;
    
    @JsonProperty("updated_by")
    private String updatedBy;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}