package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.AchievementRequest;
import com.example.vietstage_web_be.dto.response.AchievementResponse;
import com.example.vietstage_web_be.dto.response.LearnerAchievementsResponse;
import com.example.vietstage_web_be.entity.User;

import java.util.List;

public interface IAchievementService {
    List<AchievementResponse> getAllAchievements();
    AchievementResponse createAchievement(AchievementRequest request);
    AchievementResponse updateAchievement(Long id, AchievementRequest request);
    LearnerAchievementsResponse getMyAchievements(User learner);
    void revokeAchievement(Long learnerId, Long achievementId);
}