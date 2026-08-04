package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.InstructorPracticeAttemptRequest;
import com.example.vietstage_web_be.dto.response.LearnerSummaryResponse;
import com.example.vietstage_web_be.dto.response.PracticeAttemptDetailResponse;
import com.example.vietstage_web_be.dto.response.PracticeAttemptGroupedResponse;
import com.example.vietstage_web_be.entity.LearnerProfile;
import com.example.vietstage_web_be.entity.PracticeAttempt;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.LessonCompletionRepository;
import com.example.vietstage_web_be.repository.PracticeAttemptRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.service.IInstructorService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorServiceImpl implements IInstructorService {
    private final PracticeAttemptRepository practiceAttemptRepository;
    private final LessonCompletionRepository lessonCompletionRepository;
    private final UserRepository userRepository;

    @Override
    public LearnerSummaryResponse getLearnerProgressSummary(Long learnerId) {
        User learner = userRepository.findById(learnerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        LearnerProfile profile = learner.getLearnerProfile();

        Long totalCompletedLessons = lessonCompletionRepository.countCompletedLessonsByLearnerId(learnerId);

        List<Object[]> rawGrouped = practiceAttemptRepository.findGroupedPracticeStats(learnerId, null, null, null, "day");

        long totalAttempts = 0;
        double sumScore = 0;

        for (Object[] row : rawGrouped) {
            long attempts = ((Number) row[1]).longValue();
            double avgScore = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

            totalAttempts += attempts;
            sumScore += avgScore * attempts;
        }

        BigDecimal avgPracticeScore = totalAttempts > 0
                ? BigDecimal.valueOf(sumScore / totalAttempts).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Long totalPracticeSeconds = (profile != null && profile.getTotalPracticeSeconds() != null) ? profile.getTotalPracticeSeconds() : 0L;
        Integer currentStreak = (profile != null && profile.getCurrentStreak() != null) ? profile.getCurrentStreak() : 0;

        return LearnerSummaryResponse.builder()
                .learnerId(learner.getId())
                .learnerName(learner.getFullName())
                .totalLessonsCompleted(totalCompletedLessons)
                .totalPracticeAttempts(totalAttempts)
                .averagePracticeScore(avgPracticeScore)
                .currentStreakDays(currentStreak)
                .totalPracticeDurationMinutes(totalPracticeSeconds)
                .build();
    }

    @Override
    public Page<PracticeAttemptDetailResponse> getFilteredPracticeAttemptDetail(InstructorPracticeAttemptRequest request) {
        LocalDateTime fromDateTime = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = request.getToDate() != null ? request.getToDate().atTime(LocalTime.MAX) : null;

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<PracticeAttempt> attemptPage = practiceAttemptRepository.findFilteredAttempts(
                request.getLearnerId(),
                request.getLessonId(),
                fromDateTime,
                toDateTime,
                pageable
        );

        return attemptPage.map(attempt -> PracticeAttemptDetailResponse.builder()
                .attemptId(attempt.getId())
                .learnerId(attempt.getLearner() != null ? attempt.getLearner().getId() : null)
                .learnerName(attempt.getLearner() != null ? attempt.getLearner().getFullName() : null)
                .lessonId((attempt.getExercise() != null && attempt.getExercise().getLesson() != null)
                        ? attempt.getExercise().getLesson().getId() : null)
                .lessonTitle((attempt.getExercise() != null && attempt.getExercise().getLesson() != null)
                        ? attempt.getExercise().getLesson().getTitle() : null)
                .exerciseId(attempt.getExercise() != null ? attempt.getExercise().getId() : null)
                .exerciseTitle(attempt.getExercise() != null ? attempt.getExercise().getTitle() : null)
                .pitchScore(attempt.getPitchScore())
                .rhythmScore(attempt.getRhythmScore())
                .dynamicsScore(attempt.getDynamicsScore())
                .totalScore(attempt.getTotalScore())
                .stars(attempt.getStars())
                .pointsEarned(attempt.getPointsEarned())
                .syncStatus(attempt.getSyncStatus())
                .attemptedAt(attempt.getCreatedAt())
                .build());
    }

    @Override
    public List<PracticeAttemptGroupedResponse> getGroupedPracticeAttemptDetail(@MonotonicNonNull PracticeAttemptGroupedResponse request) {
        LocalDateTime fromDateTime = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : null;
        LocalDateTime toDateTime = request.getToDate() != null ? request.getToDate().atTime(LocalTime.MAX) : null;

        String groupBy = (request.getGroupBy() != null && !request.getGroupBy().isBlank())
                ? request.getGroupBy().toLowerCase()
                : "day";

        List<Object[]> results = practiceAttemptRepository.findGroupedPracticeStats(
                request.getLearnerId(),
                request.getLessonId(),
                fromDateTime,
                toDateTime,
                groupBy
        );

        return results.stream().map(row -> {
            String timeGroup = (String) row[0];
            Long totalAttempts = ((Number) row[1]).longValue();
            BigDecimal avgTotalScore = row[2] != null
                    ? BigDecimal.valueOf(((Number) row[2]).doubleValue()).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            Long totalPointsEarned = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            Long totalStarsEarned = row[4] != null ? ((Number) row[4]).longValue() : 0L;

            return PracticeAttemptGroupedResponse.builder()
                    .timeGroup(timeGroup)
                    .totalAttempts(totalAttempts)
                    .averageTotalScore(avgTotalScore)
                    .totalPointsEarned(totalPointsEarned)
                    .totalStarsEarned(totalStarsEarned)
                    .build();
        }).collect(Collectors.toList());
    }
}
