package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeedbackRequest {
    @NotBlank(message = "Nội dung feedback không được để trống")
    @jakarta.validation.constraints.Size(max = 1000, message = "Nội dung feedback không vượt quá 1000 ký tự")
    @io.swagger.v3.oas.annotations.media.Schema(description = "Nội dung phản hồi", maxLength = 1000)
    private String comment;

    public void setComment(String comment) {
        this.comment = comment != null ? comment.trim() : null;
    }
}