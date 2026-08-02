package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    
    private Long id;
    private String title;
    private String message;
    private String type;
    
    @JsonProperty("is_read")
    private Boolean isRead;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}