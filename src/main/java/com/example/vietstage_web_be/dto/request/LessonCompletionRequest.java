package com.example.vietstage_web_be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCompletionRequest {
    private String clientAttemptId;
    private Date completedAt;
    private BigDecimal score;
}
