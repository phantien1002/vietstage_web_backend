package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MinigameAttemptRequest {
    @NotNull(message = "Score is required")
    private Integer score;

    @NotBlank(message = "Client attempt id is required")
    private String clientAttemptId;

    @NotNull(message = "Started at is required")
    private LocalDateTime startedAt;

    @NotNull(message = "Completed at is required")
    private LocalDateTime completedAt;
    
    // Dữ liệu chơi của user để server xác minh thay vì chỉ tin vào score (ví dụ: các nốt đã chọn, timing tap...)
    private String playData;
}
