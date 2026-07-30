package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AchievementResponse {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String conditionJson;
    private LocalDateTime earnedAt; // Optional, only populated for earned achievements
}
