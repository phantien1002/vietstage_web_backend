package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationListResponse {
    
    private List<NotificationResponse> data;
    
    @JsonProperty("unread_count")
    private Long unreadCount;
    
    private int page;
    private int size;
    private long total;
}