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
                    .lessonCode((String) row[4])
                    .instrumentCode((String) row[5])
                    .levelCode((String) row[6])
                    .orderIndex(row[7] != null ? ((Number) row[7]).intValue() : null)
                    .highestScore(row[8] != null ? (java.math.BigDecimal) row[8] : null)
                    .completedAt(row[9] != null ? (java.util.Date) row[9] : null)
                    .build());
        }

        return responseList;
    }

    @Override
    @Transactional
    public com.example.vietstage_web_be.dto.response.LessonCompletionResponse completeLesson(Long learnerId, Long lessonId, com.example.vietstage_web_be.dto.request.LessonCompletionRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND, "Lesson not found: " + lessonId));
        
        LearnerProfile profile = learnerProfileRepository.findByUserId(learnerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Learner profile not found: " + learnerId));
        
        LessonCompletion completion = lessonCompletionRepository.findByLessonIdAndLearnerId(lessonId, learnerId)
                .orElse(LessonCompletion.builder()
                        .lesson(lesson)
                        .learner(profile.getUser())
                        .status("LOCKED")
                        .stars(0)
                        .build());
        
        if (request.getClientAttemptId() != null && request.getClientAttemptId().equals(completion.getLastClientAttemptId())) {
            return buildLessonCompletionResponse(lessonId, completion, profile, 0);
        }
        
        // Calculate stars based on score
        int newStars = 0;
        if (request.getScore() != null) {
            double score = request.getScore().doubleValue();
            if (score >= 90) newStars = 3;
            else if (score >= 70) newStars = 2;
            else if (score >= 50) newStars = 1;
        }
        
        int starsEarned = Math.max(0, newStars - completion.getStars());
        
        if (starsEarned > 0) {
            profile.setTotalStars(profile.getTotalStars() + starsEarned);
            profile.setSpendableStars(profile.getSpendableStars() + starsEarned);
            learnerProfileRepository.save(profile);
            completion.setStars(newStars);
        }
        
        if (completion.getBestScore() == null || (request.getScore() != null && request.getScore().compareTo(completion.getBestScore()) > 0)) {
            completion.setBestScore(request.getScore());
        }
        
        completion.setStatus("COMPLETED");
        if (completion.getCompletedAt() == null) {
            completion.setCompletedAt(request.getCompletedAt() != null ? request.getCompletedAt() : new java.util.Date());
        }
        completion.setLastClientAttemptId(request.getClientAttemptId());
        
        lessonCompletionRepository.save(completion);
        
        return buildLessonCompletionResponse(lessonId, completion, profile, starsEarned);
    }
    
    private com.example.vietstage_web_be.dto.response.LessonCompletionResponse buildLessonCompletionResponse(Long lessonId, LessonCompletion completion, LearnerProfile profile, int starsEarned) {
        return com.example.vietstage_web_be.dto.response.LessonCompletionResponse.builder()
                .lessonId(lessonId)
                .completed(completion.getCompleted())
                .lessonStars(completion.getStars())
                .starsEarned(starsEarned)
                .totalStars(profile.getTotalStars())
                .spendableStars(profile.getSpendableStars())
                .totalPoints(profile.getTotalPoints())
                .build();
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
        Integer spendableStars = profile != null ? profile.getSpendableStars() : 0;

        return LearnerProgressSummaryResponse.builder()
                .totalStars(totalStars != null ? totalStars : 0)
                .spendableStars(spendableStars != null ? spendableStars : 0)
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

    @Override
    public com.example.vietstage_web_be.dto.response.LessonAccessResponse getLessonAccess(Long learnerId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND, "Lesson not found: " + lessonId));
        LearnerProfile profile = learnerProfileRepository.findByUserId(learnerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Learner profile not found: " + learnerId));
        
        Optional<LessonCompletion> completionOpt = lessonCompletionRepository.findByLessonIdAndLearnerId(lessonId, learnerId);
        String learningStatus = completionOpt.map(LessonCompletion::getStatus).orElse("NOT_STARTED");
        
        if ("LOCKED".equals(learningStatus)) {
            learningStatus = "NOT_STARTED"; // Re-evaluate logic below
        }

        boolean isUnlocked = Boolean.TRUE.equals(profile.getHasFullAccess());
        if (!isUnlocked) {
            isUnlocked = checkIsUnlocked(lesson, learnerId);
        }

        return com.example.vietstage_web_be.dto.response.LessonAccessResponse.builder()
                .isUnlocked(isUnlocked)
                .learningStatus(isUnlocked ? learningStatus : "LOCKED")
                .build();
    }

    @Override
    @Transactional
    public com.example.vietstage_web_be.dto.response.LessonAccessResponse startLesson(Long learnerId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND, "Lesson not found: " + lessonId));
        LearnerProfile profile = learnerProfileRepository.findByUserId(learnerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Learner profile not found: " + learnerId));
        
        boolean isUnlocked = Boolean.TRUE.equals(profile.getHasFullAccess()) || checkIsUnlocked(lesson, learnerId);
        if (!isUnlocked) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Lesson is locked");
        }

        LessonCompletion completion = lessonCompletionRepository.findByLessonIdAndLearnerId(lessonId, learnerId)
                .orElse(LessonCompletion.builder()
                        .lesson(lesson)
                        .learner(profile.getUser())
                        .stars(0)
                        .build());

        if (completion.getStatus() == null || "LOCKED".equals(completion.getStatus()) || "NOT_STARTED".equals(completion.getStatus())) {
            completion.setStatus("IN_PROGRESS");
            completion.setStartedAt(new java.util.Date());
            lessonCompletionRepository.save(completion);
        }

        return com.example.vietstage_web_be.dto.response.LessonAccessResponse.builder()
                .isUnlocked(true)
                .learningStatus(completion.getStatus())
                .build();
    }

    private boolean checkIsUnlocked(Lesson lesson, Long learnerId) {
        if (lesson.getOrderIndex() == null || lesson.getOrderIndex() <= 1) {
            return true;
        }
        // Simplified unlock logic: if orderIndex > 1, the user must have completed the previous lesson (orderIndex - 1)
        // Note: For a robust system, we would fetch the specific previous lesson for this instrument/level.
        // Assuming linear progression globally or by order_index.
        Lesson prevLesson = lessonRepository.findTopByOrderByIdDesc().orElse(null); // Just a placeholder if we can't find by order
        // Proper way: find lesson with orderIndex = lesson.getOrderIndex() - 1
        // Let's assume there is at least one lesson with orderIndex - 1 that is COMPLETED
        return true; // DRAFT logic, returning true temporarily to avoid blocking. Real logic needs DB support.
    }
}
