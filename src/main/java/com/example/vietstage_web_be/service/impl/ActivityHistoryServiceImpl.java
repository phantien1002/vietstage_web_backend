package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.ActivityHistoryDetailResponse;
import com.example.vietstage_web_be.dto.response.ActivityHistoryItemResponse;
import com.example.vietstage_web_be.entity.MinigameAttempt;
import com.example.vietstage_web_be.entity.PracticeAttempt;
import com.example.vietstage_web_be.entity.QuizAttempt;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.MinigameAttemptRepository;
import com.example.vietstage_web_be.repository.PracticeAttemptRepository;
import com.example.vietstage_web_be.repository.QuizAttemptRepository;
import com.example.vietstage_web_be.service.IActivityHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityHistoryServiceImpl implements IActivityHistoryService {
    private static final int MAX_SIZE = 50;
    private final QuizAttemptRepository quizAttemptRepository;
    private final MinigameAttemptRepository minigameAttemptRepository;
    private final PracticeAttemptRepository practiceAttemptRepository;

    @Override
    public Page<ActivityHistoryItemResponse> getHistory(User learner, int page, int size, String type, LocalDateTime from, LocalDateTime to) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        String normalizedType = normalizeType(type);
        int fetchSize = Math.min((safePage + 1) * safeSize, 500);
        Pageable latest = PageRequest.of(0, fetchSize, Sort.by("attemptedAt").descending());
        List<ActivityHistoryItemResponse> items = new ArrayList<>();

        if (normalizedType.isEmpty() || "QUIZ".equals(normalizedType)) {
            quizAttemptRepository.findByLearnerId(learner.getId(), latest).forEach(attempt -> items.add(mapQuiz(attempt)));
        }
        if (normalizedType.isEmpty() || "MINIGAME".equals(normalizedType)) {
            Pageable minigames = PageRequest.of(0, fetchSize, Sort.by("completedAt").descending());
            minigameAttemptRepository.findByLearnerId(learner.getId(), minigames).forEach(attempt -> items.add(mapMinigame(attempt)));
        }
        if (normalizedType.isEmpty() || "PRACTICE".equals(normalizedType)) {
            Pageable practice = PageRequest.of(0, fetchSize, Sort.by("createdAt").descending());
            practiceAttemptRepository.findByLearnerId(learner.getId(), practice).forEach(attempt -> items.add(mapPractice(attempt)));
        }

        items.removeIf(item -> (from != null && item.getCompletedAt().isBefore(from)) || (to != null && item.getCompletedAt().isAfter(to)));
        items.sort(Comparator.comparing(ActivityHistoryItemResponse::getCompletedAt).reversed());
        int start = safePage * safeSize;
        List<ActivityHistoryItemResponse> content = start >= items.size() ? List.of() : items.subList(start, Math.min(start + safeSize, items.size()));
        return new PageImpl<>(content, PageRequest.of(safePage, safeSize), items.size());
    }

    @Override
    public ActivityHistoryDetailResponse getDetail(User learner, String eventId) {
        String[] parts = eventId == null ? new String[0] : eventId.split(":", 2);
        if (parts.length != 2) throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        Long id;
        try { id = Long.valueOf(parts[1]); } catch (NumberFormatException ex) { throw new AppException(ErrorCode.RESOURCE_NOT_FOUND); }
        return switch (normalizeType(parts[0])) {
            case "QUIZ" -> detailQuiz(quizAttemptRepository.findByIdAndLearnerId(id, learner.getId()).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
            case "MINIGAME" -> detailMinigame(minigameAttemptRepository.findByIdAndLearnerId(id, learner.getId()).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
            case "PRACTICE" -> detailPractice(practiceAttemptRepository.findByIdAndLearnerId(id, learner.getId()).orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)));
            default -> throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        };
    }

    private String normalizeType(String type) {
        String value = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        return switch (value) { case "", "QUIZ", "MINIGAME", "PRACTICE" -> value; default -> throw new AppException(ErrorCode.RESOURCE_NOT_FOUND); };
    }

    private ActivityHistoryItemResponse mapQuiz(QuizAttempt a) {
        return ActivityHistoryItemResponse.builder().eventId("QUIZ:" + a.getId()).type("QUIZ")
                .lessonId(a.getQuiz().getLesson().getId()).lessonTitle(a.getQuiz().getLesson().getTitle()).title(a.getQuiz().getTitle())
                .score(a.getScore()).maxScore(BigDecimal.valueOf(100)).starsEarned(rewardStars(a.getStarsEarned()))
                .pointsEarned(a.getPointsEarned() != null ? a.getPointsEarned() : (Boolean.TRUE.equals(a.getIsCorrect()) ? 10 : 0))
                .completedAt(a.getAttemptedAt()).status("CONFIRMED").build();
    }

    private ActivityHistoryItemResponse mapMinigame(MinigameAttempt a) {
        return ActivityHistoryItemResponse.builder().eventId("MINIGAME:" + a.getId()).type("MINIGAME")
                .lessonId(a.getChallenge().getLesson().getId()).lessonTitle(a.getChallenge().getLesson().getTitle()).title(a.getChallenge().getTitle())
                .score(BigDecimal.valueOf(a.getScore())).maxScore(BigDecimal.valueOf(a.getChallenge().getMaxScore())).starsEarned(rewardStars(a.getStarsEarned()))
                .pointsEarned(a.getPointsEarned() != null ? a.getPointsEarned() : rewardStars(a.getStarsEarned()) * 5)
                .completedAt(a.getCompletedAt()).status("CONFIRMED").build();
    }

    private ActivityHistoryItemResponse mapPractice(PracticeAttempt a) {
        return ActivityHistoryItemResponse.builder().eventId("PRACTICE:" + a.getId()).type("PRACTICE")
                .lessonId(a.getExercise().getLesson().getId()).lessonTitle(a.getExercise().getLesson().getTitle()).title(a.getExercise().getTitle())
                .score(a.getTotalScore()).maxScore(BigDecimal.valueOf(100)).starsEarned(rewardStars(a.getStars()))
                .pointsEarned(a.getPointsEarned() == null ? 0 : a.getPointsEarned()).completedAt(a.getCreatedAt()).status("CONFIRMED").build();
    }

    private ActivityHistoryDetailResponse detailQuiz(QuizAttempt a) {
        return ActivityHistoryDetailResponse.builder().eventId("QUIZ:" + a.getId()).type("QUIZ").lessonTitle(a.getQuiz().getLesson().getTitle()).title(a.getQuiz().getTitle())
                .question(a.getQuiz().getQuestion()).selectedAnswer(a.getSelectedAnswer()).correctAnswer(a.getQuiz().getCorrectAnswer()).isCorrect(a.getIsCorrect())
                .score(a.getScore()).maxScore(BigDecimal.valueOf(100)).starsEarned(rewardStars(a.getStarsEarned()))
                .pointsEarned(a.getPointsEarned() != null ? a.getPointsEarned() : (Boolean.TRUE.equals(a.getIsCorrect()) ? 10 : 0)).completedAt(a.getAttemptedAt()).status("CONFIRMED").build();
    }

    private ActivityHistoryDetailResponse detailMinigame(MinigameAttempt a) {
        return ActivityHistoryDetailResponse.builder().eventId("MINIGAME:" + a.getId()).type("MINIGAME").lessonTitle(a.getChallenge().getLesson().getTitle()).title(a.getChallenge().getTitle())
                .challengeType(a.getChallenge().getChallengeType()).score(BigDecimal.valueOf(a.getScore())).maxScore(BigDecimal.valueOf(a.getChallenge().getMaxScore()))
                .starsEarned(rewardStars(a.getStarsEarned())).pointsEarned(a.getPointsEarned() != null ? a.getPointsEarned() : rewardStars(a.getStarsEarned()) * 5)
                .startedAt(a.getStartedAt()).completedAt(a.getCompletedAt()).status("CONFIRMED").build();
    }

    private ActivityHistoryDetailResponse detailPractice(PracticeAttempt a) {
        return ActivityHistoryDetailResponse.builder().eventId("PRACTICE:" + a.getId()).type("PRACTICE").lessonTitle(a.getExercise().getLesson().getTitle()).title(a.getExercise().getTitle())
                .score(a.getTotalScore()).maxScore(BigDecimal.valueOf(100)).pitchScore(a.getPitchScore()).rhythmScore(a.getRhythmScore()).dynamicsScore(a.getDynamicsScore())
                .tonalQualityScore(a.getTonalQualityScore()).breathScore(a.getBreathScore()).starsEarned(rewardStars(a.getStars())).pointsEarned(a.getPointsEarned() == null ? 0 : a.getPointsEarned())
                .completedAt(a.getCreatedAt()).status("CONFIRMED").build();
    }

    private int rewardStars(Integer value) { return value == null ? 0 : value; }
}
