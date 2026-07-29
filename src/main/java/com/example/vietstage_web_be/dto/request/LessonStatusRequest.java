package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body cho PUT /api/Lesson/{id}/status
 * status: PENDING | APPROVED | REJECTED
 * comment: bắt buộc khi status = REJECTED
 */
@Getter
@Setter
public class LessonStatusRequest {

    @NotBlank(message = "Status cannot be blank")
    private String status;

    private String comment;
}

