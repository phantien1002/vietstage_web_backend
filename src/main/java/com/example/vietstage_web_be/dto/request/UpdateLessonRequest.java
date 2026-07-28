package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UpdateLessonRequest {

    @NotBlank(message = "Lesson title cannot be blank")
    private String title;

    private String description;

    private Integer orderIndex;
    private Long skillLevelId;
    private List<String> exercises;
    private BigDecimal passThreshold;
}
