package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponse {
    private Long id;
    private String title;
    private String difficulty;
    private InstrumentInfo instrument;
    private CreatorInfo createdBy;
    private List<TechniqueInfo> techniques;
    private List<ContentInfo> contents;
    private List<AudioInfo> audioReferences;
    private List<ExerciseInfo> exercises;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InstrumentInfo {
        private Long id;
        private String name;
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
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContentInfo {
        private Long id;
        private String contentText;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AudioInfo {
        private Long id;
        private String audioUrl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExerciseInfo {
        private Long id;
        private String title;
    }
}
