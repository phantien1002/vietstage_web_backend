package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponse {
    private Long id;
    private String lessonCode;
    private String title;
    private String description;
    private String status;
    private Integer orderIndex;
    private SkillLevelInfo skillLevel;
    private InstrumentInfo instrument;
    private CreatorInfo createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TechniqueInfo> techniques;
    private List<AssetInfo> mediaAssets;
    private List<ExerciseInfo> exercises;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkillLevelInfo {
        private Long id;
        private String levelName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InstrumentInfo {
        private Long id;
        private String instrumentCode;
        private String name;
        private String iconUrl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreatorInfo {
        private Long id;

        private String fullName;
        private String role;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TechniqueInfo {
        private Long id;
        private String name;
        private String guideUrl;
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AssetInfo {
        private Long id;
        private String assetType;
        private String assetUrl;
        private Integer tempoBpm;
        private BigDecimal durationSec;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExerciseInfo {
        private Long id;
        private String title;
        private String description;
        private BigDecimal passThreshold;
        private Integer orderIndex;
    }
}

