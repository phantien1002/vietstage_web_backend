package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkillLevelResponse {
    private Long id;
    private String levelCode;
    private String levelName;
    private Short orderIndex;
}
