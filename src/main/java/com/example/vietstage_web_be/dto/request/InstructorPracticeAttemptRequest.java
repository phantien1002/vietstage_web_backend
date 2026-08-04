package com.example.vietstage_web_be.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class InstructorPracticeAttemptRequest {
    private Long learnerId;
    private Long lessonId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private String groupBy;

    private Integer page = 0;
    private Integer size = 10;
}
