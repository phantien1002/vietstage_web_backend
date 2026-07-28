package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class LessonRequest {
    @NotBlank(message = "Lesson title cannot be blank")
    private String title;

    private String description;

    // v2.0: skill_level_id thay thế difficulty string
    private Long skillLevelId;

    @NotNull(message = "Instrument ID cannot be null")
    private Long instrumentId;

    // DRAFT | PENDING | APPROVED | REJECTED (default DRAFT)
    private String status;

    private Integer orderIndex;

    private Set<Long> techniqueIds;

    // v2.0: lesson_assets thay thế audio_references
    private List<LessonAssetRequest> assets;

    // Danh sách bài tập (tiêu đề đơn giản)
    private List<String> exercises;

    @Getter
    @Setter
    public static class LessonAssetRequest {
        // REFERENCE_AUDIO | SHEET_IMAGE | TECHNIQUE_VIDEO | BEAT_MAP
        private String assetType;
        private String assetUrl;
        private Integer tempoBpm;
        private BigDecimal durationSec;
    }
}
