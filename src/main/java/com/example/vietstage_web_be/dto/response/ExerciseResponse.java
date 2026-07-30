package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResponse {
    private Long id;

    private Long lessonId;

    private String title;

    private String description;

    private Long beatMapAssetId;

    private Double passThreshold;

    private Integer orderIndex;
}
