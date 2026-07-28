package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateExerciseRequest {
    @NotBlank
    private String title;

    private String description;

    private Long beatMapAssetId;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private Double passThreshold;

    @Min(0)
    private Integer orderIndex;
}
