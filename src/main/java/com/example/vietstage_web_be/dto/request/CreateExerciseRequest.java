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

    @io.swagger.v3.oas.annotations.media.Schema(description = "Ngưỡng đạt (0-100)", minimum = "0.00", maximum = "100.00")
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private Double passThreshold;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Thứ tự bài tập", minimum = "1")
    @Min(1)
    private Integer orderIndex;
}
