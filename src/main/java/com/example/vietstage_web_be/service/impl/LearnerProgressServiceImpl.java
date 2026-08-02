package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.InstructorLearnerProgressResponse;
import com.example.vietstage_web_be.dto.response.LearnerProgressItemResponse;
import com.example.vietstage_web_be.dto.response.LearnerProgressSummaryResponse;
import com.example.vietstage_web_be.entity.LessonCompletion;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.LearnerProfile;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.LessonCompletionRepository;
import com.example.vietstage_web_be.repository.LearnerProfileRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.PracticeAttemptRepository;
import com.example.vietstage_web_be.repository.QuizAttemptRepository;
import com.example.vietstage_web_be.service.ILearnerProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearnerProgressServiceImpl implements ILearnerProgressService {
    private final LessonCompletionRepository lessonCompletionRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final PracticeAttemptRepository practiceAttemptRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final LessonRepository lessonRepository;

    @Override
    public List<LearnerProgressItemResponse> getLearnerProgress(Long learnerId, Long instrumentId, Long skillLevelId) {
        List<Object[]> rawResult = lessonCompletionRepository.findLearnerProgressList(learnerId, instrumentId, skillLevelId);
        List<LearnerProgressItemResponse> responseList = new ArrayList<>();

        for (Object[] row : rawResult) {
            responseList.add(LearnerProgressItemResponse.builder()
                    .lessonId((Long) row[0])
                    .title((String) row[1])
                    .stars(((Number) row[2]).intValue())
                    .completed((Boolean) row[3])
                    .build());
        }

        return responseList;
    }

    @Override
    @Transactional
    public LearnerProfile updateStreakAndSave(LearnerProfile profile) {
        LocalDate today = LocalDate.now();
        LocalDate lastDate = profile.getLastPracticeDate();

        if (lastDate == null) {
            profile.setCurrentStreak(1);
        } else if (lastDate.equals(today.minusDays(1))) {
            profile.setCurrentStreak(profile.getCurrentStreak() + 1);
        } else if (lastDate.isBefore(today.minusDays(1))) {
            profile.setCurrentStreak(1);
        }
        // If lastDate == today, streak remains unchanged

        if (profile.getCurrentStreak() > profile.getLongestStreak()) {
            profile.setLongestStreak(profile.getCurrentStreak());
        }

        profile.setLastPracticeDate(today);
        return learnerProfileRepository.save(profile);
    }

    @Override
    public LearnerProgressSummaryResponse getLearnerProgressSummary(Long learnerId) {
        Integer totalStars = lessonCompletionRepository.sumTotalStarsByLearnerId(learnerId);
        Long completedLessons = lessonCompletionRepository.countCompletedLessonsByLearnerId(learnerId);

        LearnerProfile profile = learnerProfileRepository.findByUserId(learnerId).orElse(null);
        Integer currentStreak = profile != null ? profile.getCurrentStreak() : 0;
        Integer longestStreak = profile != null ? profile.getLongestStreak() : 0;
        Integer totalPoints = profile != null ? profile.getTotalPoints() : 0;

        return LearnerProgressSummaryResponse.builder()
                .totalStars(totalStars != null ? totalStars : 0)
                .completedLessons(completedLessons != null ? completedLessons : 0L)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .totalPoints(totalPoints)
                .adaptiveDifficulty(0)
                .build();
    }

    @Override
    public InstructorLearnerProgressResponse getLearnerProgressByInstructor(Long lessonId, Long learnerId, Long instructorId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND, "lesson not found with id: " + lessonId));

        if (lesson.getCreatedBy() != null && !lesson.getCreatedBy().equals(instructorId)) {
            throw new AppException(ErrorCode.INSTRUCTOR_FORBIDDEN);
        }

        Optional<LessonCompletion> completionsOptional = lessonCompletionRepository.findByLessonIdAndLearnerId(lessonId, learnerId);

        Integer PracticeAttempt = practiceAttemptRepository.countAttemptsByLessonAndLearner(lessonId, learnerId);
        Double bestScore = practiceAttemptRepository.findBestScoreByLessonAndLearner(lessonId, learnerId);
        Integer quizAttempt = quizAttemptRepository.countQuizAttemptsByLessonAndLearner(lessonId, learnerId);

        return InstructorLearnerProgressResponse.builder()
                .lessonId(lessonId)
                .learnerId(learnerId)
                .stars(completionsOptional.map(LessonCompletion::getStars).orElse(0))
                .completed(completionsOptional.map(LessonCompletion::getCompleted).orElse(false))
                .totalPracticeAttempts(PracticeAttempt != null ? PracticeAttempt : 0)
                .bestPracticeScore(bestScore !=  null ? bestScore : 0.0)
                .totalQuizAttempts(quizAttempt != null ? quizAttempt : 0)
                .build();
    }
}
