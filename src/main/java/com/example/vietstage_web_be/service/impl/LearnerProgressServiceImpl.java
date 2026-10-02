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
        Integer lessonStars = lessonCompletionRepository.sumTotalStarsByLearnerId(learnerId);
        Long completedLessons = lessonCompletionRepository.countCompletedLessonsByLearnerId(learnerId);

        LearnerProfile profile = learnerProfileRepository.findByUserId(learnerId).orElse(null);
        // Quiz/Mini Game rewards live in learner_profiles, while lesson stars
        // come from learner_lesson_progress. Use the wallet total as the
        // canonical summary so refreshing after a quiz cannot erase its stars.
        int profileStars = profile != null && profile.getTotalStars() != null ? profile.getTotalStars() : 0;
        int totalStars = profile != null
                ? Math.max(lessonStars != null ? lessonStars : 0, profileStars)
                : (lessonStars != null ? lessonStars : 0);
        Integer currentStreak = profile != null ? profile.getCurrentStreak() : 0;
        Integer longestStreak = profile != null ? profile.getLongestStreak() : 0;
        Integer totalPoints = profile != null ? profile.getTotalPoints() : 0;
        Integer spendableStars = profile != null ? profile.getSpendableStars() : 0;

        return LearnerProgressSummaryResponse.builder()
                .totalStars(totalStars)
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

        Optional<LessonCompletion> completionsOptional = lessonCompletionRepository.findByLessonIdAndLearnerId(lessonId, learnerId);

        Integer PracticeAttempt = practiceAttemptRepository.countAttemptsByLessonAndLearner(lessonId, learnerId);
        Double bestScore = practiceAttemptRepository.findBestScoreByLessonAndLearner(lessonId, learnerId);
        Integer quizAttempt = quizAttemptRepository.countQuizAttemptsByLessonAndLearner(lessonId, learnerId);

        LearnerProfile profile = learnerProfileRepository.findByUserId(learnerId).orElse(null);
        boolean isUnlocked = false;
        if (profile != null && Boolean.TRUE.equals(profile.getHasFullAccess())) {
            isUnlocked = true;
        } else {
            isUnlocked = checkIsUnlocked(lesson, learnerId);
        }

        String learningStatus = completionsOptional.map(LessonCompletion::getStatus).orElse("NOT_STARTED");
        if ("LOCKED".equals(learningStatus)) {
            learningStatus = "NOT_STARTED";
        }
        if (!isUnlocked) {
            learningStatus = "LOCKED";
        }

        return InstructorLearnerProgressResponse.builder()
                .lessonId(lessonId)
                .learnerId(learnerId)
                .isUnlocked(isUnlocked)
                .learningStatus(learningStatus)
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
        
        Optional<Lesson> prevLessonOpt = lessonRepository.findAll().stream()
                .filter(l -> l.getInstrument().getId().equals(lesson.getInstrument().getId()) 
                          && l.getOrderIndex() != null 
                          && l.getOrderIndex() < lesson.getOrderIndex())
                .max(java.util.Comparator.comparing(Lesson::getOrderIndex));

        if (prevLessonOpt.isEmpty()) {
            return true; // No previous lesson means it's the first one practically
        }

        Lesson prevLesson = prevLessonOpt.get();
        Optional<LessonCompletion> prevCompletion = lessonCompletionRepository.findByLessonIdAndLearnerId(prevLesson.getId(), learnerId);

        return prevCompletion.isPresent() && "COMPLETED".equals(prevCompletion.get().getStatus());
    }

    @Override
    public com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse getCourseProgress(Long learnerId) {
        LearnerProfile profile = learnerProfileRepository.findByUserId(learnerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Learner profile not found: " + learnerId));
        
        boolean hasFullAccess = Boolean.TRUE.equals(profile.getHasFullAccess());
        
        List<Lesson> allLessons = lessonRepository.findAll();
        List<LessonCompletion> completions = lessonCompletionRepository.findByLearnerId(learnerId);
        
        java.util.Map<Long, LessonCompletion> completionMap = new java.util.HashMap<>();
        for (LessonCompletion c : completions) {
            completionMap.put(c.getLesson().getId(), c);
        }
        
        List<com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse.LearnerLessonProgressDTO> lessonDtos = new java.util.ArrayList<>();
        
        java.util.Map<Long, Integer> levelEarnedStars = new java.util.HashMap<>();
        java.util.Map<Long, Integer> levelTotalStars = new java.util.HashMap<>();
        java.util.Map<Long, Boolean> levelUnlocked = new java.util.HashMap<>();
        java.util.Map<Long, Boolean> levelAnyStarted = new java.util.HashMap<>();
        java.util.Map<Long, Boolean> levelAllCompleted = new java.util.HashMap<>();
        
        for (Lesson lesson : allLessons) {
            if (lesson.getSkillLevel() != null) {
                Long lvlId = lesson.getSkillLevel().getId();
                levelEarnedStars.putIfAbsent(lvlId, 0);
                levelTotalStars.putIfAbsent(lvlId, 0);
                levelUnlocked.putIfAbsent(lvlId, false);
                levelAnyStarted.putIfAbsent(lvlId, false);
                levelAllCompleted.putIfAbsent(lvlId, true);
            }
        }
        
        for (Lesson lesson : allLessons) {
            LessonCompletion completion = completionMap.get(lesson.getId());
            String status = completion != null && completion.getStatus() != null ? completion.getStatus() : "NOT_STARTED";
            
            if ("LOCKED".equals(status)) {
                status = "NOT_STARTED";
            }
            
            boolean isUnlocked = hasFullAccess || checkIsUnlocked(lesson, learnerId);
            String finalStatus = isUnlocked ? status : "LOCKED";
            int stars = completion != null && completion.getStars() != null ? completion.getStars() : 0;
            
            lessonDtos.add(com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse.LearnerLessonProgressDTO.builder()
                    .lessonId(lesson.getId())
                    .isUnlocked(isUnlocked)
                    .learningStatus(finalStatus)
                    .completed(completion != null ? Boolean.TRUE.equals(completion.getCompleted()) : false)
                    .completedAt(completion != null ? completion.getCompletedAt() : null)
                    .lessonStars(stars)
                    .highestScore(completion != null ? completion.getBestScore() : null)
                    .build());
                    
            if (lesson.getSkillLevel() != null) {
                Long lvlId = lesson.getSkillLevel().getId();
                levelTotalStars.put(lvlId, levelTotalStars.get(lvlId) + 3); // Max 3 stars per lesson
                levelEarnedStars.put(lvlId, levelEarnedStars.get(lvlId) + stars);
                
                if (isUnlocked) {
                    levelUnlocked.put(lvlId, true);
                }
                if ("IN_PROGRESS".equals(finalStatus) || "COMPLETED".equals(finalStatus)) {
                    levelAnyStarted.put(lvlId, true);
                }
                if (!"COMPLETED".equals(finalStatus)) {
                    levelAllCompleted.put(lvlId, false);
                }
            }
        }
        
        List<com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse.LearnerLevelProgressDTO> levelDtos = new java.util.ArrayList<>();
        for (Long lvlId : levelTotalStars.keySet()) {
            String lvlStatus = "LOCKED";
            if (levelUnlocked.get(lvlId)) {
                if (levelAllCompleted.get(lvlId)) {
                    lvlStatus = "COMPLETED";
                } else if (levelAnyStarted.get(lvlId)) {
                    lvlStatus = "IN_PROGRESS";
                } else {
                    lvlStatus = "NOT_STARTED";
                }
            }
            
            levelDtos.add(com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse.LearnerLevelProgressDTO.builder()
                    .levelId(lvlId)
                    .isUnlocked(levelUnlocked.get(lvlId))
                    .learningStatus(lvlStatus)
                    .earnedStars(levelEarnedStars.get(lvlId))
                    .totalStars(levelTotalStars.get(lvlId))
                    .build());
        }
        
        return com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse.builder()
                .lessons(lessonDtos)
                .levels(levelDtos)
                .build();
    }
}
