package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.ActivityStatisticsResponse;
import com.example.vietstage_web_be.entity.User;

public interface IStatisticsService {
    ActivityStatisticsResponse getQuizStatistics(Long quizId, User actor);
    ActivityStatisticsResponse getMinigameStatistics(Long minigameId, User actor);
    ActivityStatisticsResponse getLessonQuizStatistics(Long lessonId, User actor);
    ActivityStatisticsResponse getLessonMinigameStatistics(Long lessonId, User actor);
}
