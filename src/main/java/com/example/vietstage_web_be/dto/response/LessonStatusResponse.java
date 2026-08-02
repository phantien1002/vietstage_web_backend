package com.example.vietstage_web_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response gọn cho PUT /api/Lesson/{id}/status
 * Chỉ trả về {id, status} theo spec
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonStatusResponse {
    private Long id;
    private String status;
}

