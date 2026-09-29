package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.ActivityStatisticsResponse;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.MinigameChallenge;
import com.example.vietstage_web_be.entity.Quiz;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.MinigameAttemptRepository;
import com.example.vietstage_web_be.repository.MinigameChallengeRepository;
import com.example.vietstage_web_be.repository.QuizAttemptRepository;
import com.example.vietstage_web_be.repository.QuizRepository;
import com.example.vietstage_web_be.service.IStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private final QuizRepository quizRepository;
    private final MinigameChallengeRepository minigameChallengeRepository;
    private final LessonRepository lessonRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final MinigameAttemptRepository minigameAttemptRepository;

    @Override
    public ActivityStatisticsResponse getQuizStatistics(Long quizId, User actor) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));
        validateOwnership(actor, quiz.getLesson());
        
        Map<String, Object> stats = quizAttemptRepository.getQuizStatistics(quizId);
        return mapQuizStats(stats);
    }

    @Override
    public ActivityStatisticsResponse getMinigameStatistics(Long minigameId, User actor) {
        MinigameChallenge minigame = minigameChallengeRepository.findById(minigameId)
                .orElseThrow(() -> new AppException(ErrorCode.MINIGAME_NOT_FOUND));
        validateOwnership(actor, minigame.getLesson());
        
        Map<String, Object> stats = minigameAttemptRepository.getMinigameStatistics(minigameId);
        return mapMinigameStats(stats);
    }

    @Override
    public ActivityStatisticsResponse getLessonQuizStatistics(Long lessonId, User actor) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        validateOwnership(actor, lesson);
        
        Map<String, Object> stats = quizAttemptRepository.getQuizStatisticsByLesson(lessonId);
        return mapQuizStats(stats);
    }

    @Override
    public ActivityStatisticsResponse getLessonMinigameStatistics(Long lessonId, User actor) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        validateOwnership(actor, lesson);
        
        Map<String, Object> stats = minigameAttemptRepository.getMinigameStatisticsByLesson(lessonId);
        return mapMinigameStats(stats);
    }

    private ActivityStatisticsResponse mapQuizStats(Map<String, Object> stats) {
        if (stats == null || stats.get("totalAttempts") == null || ((Number) stats.get("totalAttempts")).longValue() == 0) {
            return ActivityStatisticsResponse.builder().learnersCount(0L).totalAttempts(0L).averageScore(0.0).passRate(0.0).build();
        }
        return ActivityStatisticsResponse.builder()
                .learnersCount(((Number) stats.get("learnersCount")).longValue())
                .totalAttempts(((Number) stats.get("totalAttempts")).longValue())
                .averageScore(((Number) stats.get("averageScore")).doubleValue())
                .passRate(((Number) stats.get("passRate")).doubleValue())
                .build();
    }

    private ActivityStatisticsResponse mapMinigameStats(Map<String, Object> stats) {
        if (stats == null || stats.get("totalAttempts") == null || ((Number) stats.get("totalAttempts")).longValue() == 0) {
            return ActivityStatisticsResponse.builder().learnersCount(0L).totalAttempts(0L).averageScore(0.0).averageStars(0.0).build();
        }
        return ActivityStatisticsResponse.builder()
                .learnersCount(((Number) stats.get("learnersCount")).longValue())
                .totalAttempts(((Number) stats.get("totalAttempts")).longValue())
                .averageScore(((Number) stats.get("averageScore")).doubleValue())
                .averageStars(((Number) stats.get("averageStars")).doubleValue())
                .build();
    }

    private void validateOwnership(User actor, Lesson lesson) {
        if (actor != null && actor.getRole() != null && "ADMIN".equalsIgnoreCase(actor.getRole().getName())) {
            return;
        }
        if (actor == null || lesson.getCreatedBy() == null || !actor.getId().equals(lesson.getCreatedBy().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_LESSON_ACCESS);
        }
    }
}
