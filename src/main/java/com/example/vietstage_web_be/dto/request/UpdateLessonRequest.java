package com.example.vietstage_web_be.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLessonRequest {

    @NotBlank(message = "Lesson title cannot be blank")
    private String title;

    private String description;

    @JsonProperty("order_index")
    private Integer orderIndex;

    @JsonProperty("skill_level_id")
    private Long skillLevelId;
}
