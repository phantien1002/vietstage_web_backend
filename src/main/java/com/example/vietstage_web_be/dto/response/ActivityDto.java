package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityDto {
    private String title;
    private String time;
}
