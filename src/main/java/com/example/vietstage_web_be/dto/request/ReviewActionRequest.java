package com.example.vietstage_web_be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewActionRequest {
    @NotBlank(message = "Phản hồi từ chối là bắt buộc")
    @Size(max = 1000, message = "Phản hồi không được vượt quá 1000 ký tự")
    private String feedback;

    public void setFeedback(String feedback) {
        this.feedback = feedback != null ? feedback.trim() : null;
    }
}
