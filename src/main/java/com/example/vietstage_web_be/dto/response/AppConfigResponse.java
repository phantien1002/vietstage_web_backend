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
    
    private String valueType;
    private Double min;
    private Double max;
    private Double step;
    private String options;
    private String defaultValue;
    
    @JsonProperty("config_group")
    private String configGroup;
    
    @JsonProperty("updated_by")
    private String updatedBy;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    private Long version;
}