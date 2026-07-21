package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerProgressItemResponse {
    private Long lessonId;
    private String title;
    private Integer stars;
    private Boolean completed;
}
