package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.InstructorPracticeAttemptRequest;
import com.example.vietstage_web_be.dto.response.LearnerProgressSummaryResponse;
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
    public LearnerProgressSummaryResponse getLearnerProgressSummary(Long learnerId) {
        User learner = userRepository.findById(learnerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        LearnerProfile profile = learner.getLearnerProfile();
        Long completedLessons = lessonCompletionRepository.countCompletedLessonsByLearnerId(learnerId);

        return LearnerProgressSummaryResponse.builder()
                .totalStars(profile != null && profile.getTotalStars() != null ? profile.getTotalStars() : 0)
                .completedLessons(completedLessons != null ? completedLessons : 0L)
                .currentStreak(profile != null && profile.getCurrentStreak() != null ? profile.getCurrentStreak() : 0)
                .longestStreak(profile != null && profile.getLongestStreak() != null ? profile.getLongestStreak() : 0)
                .totalPoints(profile != null && profile.getTotalPoints() != null ? profile.getTotalPoints() : 0)
                .adaptiveDifficulty(1)
                .build();
    }

    @Override
    public Page<PracticeAttemptDetailResponse> getFilteredPracticeAttempts(Long instructorId, InstructorPracticeAttemptRequest request) {
        LocalDateTime fromDateTime = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime toDateTime = request.getToDate() != null ? request.getToDate().atTime(LocalTime.MAX) : LocalDateTime.of(2100, 12, 31, 23, 59);

        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10
        );

        Page<PracticeAttempt> attemptPage = practiceAttemptRepository.findFilteredAttempts(
                instructorId,
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
                .createdAt(attempt.getCreatedAt())
                .build());
    }

    @Override
    public List<PracticeAttemptGroupedResponse> getGroupedPracticeAttemptDetail(Long instructorId, InstructorPracticeAttemptRequest request) {
        LocalDateTime fromDateTime = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime toDateTime = request.getToDate() != null ? request.getToDate().atTime(LocalTime.MAX) : LocalDateTime.of(2100, 12, 31, 23, 59);

        String groupBy = (request.getGroupBy() != null && !request.getGroupBy().isBlank())
                ? request.getGroupBy().toLowerCase()
                : "day";

        List<Object[]> results = practiceAttemptRepository.findGroupedPracticeStats(
                instructorId,
                request.getLearnerId(),
                request.getLessonId(),
                fromDateTime,
                toDateTime,
                groupBy
        );

        return results.stream().map(row -> {
            String timeGroup = (String) row[0];
            Long totalAttempts = row[1] != null ? ((Number) row[1]).longValue() : 0L;
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

    @Override
    public Page<com.example.vietstage_web_be.dto.response.LearnerForInstructorResponse> getLearnersForInstructor(Long instructorId, String search, Pageable pageable) {
        Page<User> learners = userRepository.findLearnersForInstructor(instructorId, search, pageable);
        return learners.map(user -> {
            String instrumentName = "N/A"; // favoriteInstrument is not mapped
            return com.example.vietstage_web_be.dto.response.LearnerForInstructorResponse.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .userCode(user.getUserCode())
                    .instrumentName(instrumentName)
                    .build();
        });
    }
}

