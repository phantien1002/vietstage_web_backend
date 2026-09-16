package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class LessonConfigResponse {
    private String lessonCode;
    private Integer revision;
    private List<ActivityConfigDto> activities;

    @Data
    @Builder
    public static class ActivityConfigDto {
        private String activityCode;
        private String activityType;
        private Integer orderIndex;
        private String contentText;
        private String config; // raw json string
    }
}
