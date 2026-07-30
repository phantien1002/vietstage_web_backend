package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LearnerAchievementsResponse {
    private List<AchievementResponse> earned;
    private List<AchievementResponse> locked;
}
