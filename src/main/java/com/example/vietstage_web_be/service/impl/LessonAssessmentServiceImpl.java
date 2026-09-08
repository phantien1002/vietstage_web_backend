package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.*;
import com.example.vietstage_web_be.dto.response.LessonAssessmentResponse;
import com.example.vietstage_web_be.entity.*;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.*;
import com.example.vietstage_web_be.service.ILeaderboardService;
import com.example.vietstage_web_be.service.ILessonAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonAssessmentServiceImpl implements ILessonAssessmentService {
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final MinigameChallengeRepository minigameChallengeRepository;
    private final LessonAssessmentSessionRepository sessionRepository;
    private final LessonCompletionRepository completionRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final AppConfigRepository appConfigRepository;
    private final ILeaderboardService leaderboardService;

    @Override
    @Transactional
    public LessonAssessmentResponse submit(Long lessonId, LessonAssessmentRequest request, User learner) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        if (!"PUBLISHED".equalsIgnoreCase(lesson.getStatus())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Bài học chưa được xuất bản");
        }
        Optional<LessonAssessmentSession> existing = sessionRepository
                .findByLearnerIdAndClientSessionId(learner.getId(), request.getClientSessionId());
        if (existing.isPresent()) {
            LessonAssessmentSession session = existing.get();
            if (!session.getLesson().getId().equals(lessonId)) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Mã session đã được dùng cho bài học khác");
            }
            return response(session, profile(learner), completionRepository.findByLessonIdAndLearnerId(lessonId, learner.getId()).orElse(null));
        }

        List<Quiz> quizzes = quizRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        List<MinigameChallenge> challenges = minigameChallengeRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        validateCompleteSet(quizzes.stream().map(Quiz::getId).collect(Collectors.toSet()), request.getQuizAnswers(), AssessmentQuizAnswerRequest::getQuizId, "quiz");
        validateCompleteSet(challenges.stream().map(MinigameChallenge::getId).collect(Collectors.toSet()), request.getMinigameResults(), AssessmentMinigameResultRequest::getChallengeId, "mini-game");

        Map<Long, AssessmentQuizAnswerRequest> answers = request.getQuizAnswers().stream().collect(Collectors.toMap(AssessmentQuizAnswerRequest::getQuizId, value -> value));
        Map<Long, AssessmentMinigameResultRequest> results = request.getMinigameResults().stream().collect(Collectors.toMap(AssessmentMinigameResultRequest::getChallengeId, value -> value));
        LessonAssessmentSession session = LessonAssessmentSession.builder()
                .learner(learner).lesson(lesson).clientSessionId(request.getClientSessionId()).status("CONFIRMED")
                .score(BigDecimal.ZERO).maxScore(BigDecimal.ZERO).accuracy(BigDecimal.ZERO)
                .starsEarned(0).pointsEarned(0).startedAt(request.getStartedAt()).completedAt(LocalDateTime.now()).build();
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalMax = BigDecimal.ZERO;
        for (Quiz quiz : quizzes) {
            String selected = answers.get(quiz.getId()).getSelectedAnswer().trim();
            boolean correct = answersMatch(quiz, selected);
            BigDecimal max = BigDecimal.valueOf(100);
            BigDecimal score = correct ? max : BigDecimal.ZERO;
            session.getQuizAnswers().add(LessonAssessmentQuizAnswer.builder().session(session).quiz(quiz)
                    .selectedAnswer(selected).correct(correct).score(score).maxScore(max).build());
            totalScore = totalScore.add(score); totalMax = totalMax.add(max);
        }
        for (MinigameChallenge challenge : challenges) {
            AssessmentMinigameResultRequest result = results.get(challenge.getId());
            int max = Optional.ofNullable(challenge.getMaxScore()).orElse(0);
            if (result.getScore() < 0 || result.getScore() > max) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Điểm mini-game không hợp lệ");
            }
            session.getMinigameResults().add(LessonAssessmentMinigameResult.builder().session(session).minigame(challenge)
                    .score(BigDecimal.valueOf(result.getScore())).maxScore(BigDecimal.valueOf(max))
                    .startedAt(result.getStartedAt()).completedAt(result.getCompletedAt()).build());
            totalScore = totalScore.add(BigDecimal.valueOf(result.getScore())); totalMax = totalMax.add(BigDecimal.valueOf(max));
        }
        if (totalMax.signum() <= 0) throw new AppException(ErrorCode.BAD_REQUEST, "Bài học không có nội dung đánh giá hợp lệ");
        BigDecimal accuracy = totalScore.multiply(BigDecimal.valueOf(100)).divide(totalMax, 2, RoundingMode.HALF_UP);
        int lessonStars = starsFor(accuracy);
        LearnerProfile profile = profile(learner);
        LessonCompletion completion = completionRepository.findByLessonIdAndLearnerId(lessonId, learner.getId()).orElseGet(() -> LessonCompletion.builder()
                .learner(learner).lesson(lesson).status("LOCKED").stars(0).updatedAt(new Date()).build());
        int oldStars = Optional.ofNullable(completion.getStars()).orElse(0);
        int deltaStars = Math.max(0, lessonStars - oldStars);
        int pointsPerStar = configInt("scoring.lesson_assessment.points_per_star", 10);
        int deltaPoints = deltaStars * pointsPerStar;
        if (deltaPoints > 0) leaderboardService.addPoints(learner, deltaPoints, "LESSON_ASSESSMENT");
        profile = profile(learner);
        if (deltaStars > 0) {
            profile.setTotalStars(profile.getTotalStars() + deltaStars);
            profile.setSpendableStars(profile.getSpendableStars() + deltaStars);
            learnerProfileRepository.save(profile);
            completion.setStars(lessonStars);
        }
        if (lessonStars >= 1) {
            completion.setStatus("COMPLETED");
            if (completion.getCompletedAt() == null) completion.setCompletedAt(new Date());
        }
        if (completion.getBestScore() == null || accuracy.compareTo(completion.getBestScore()) > 0) completion.setBestScore(accuracy);
        completion.setLastClientAttemptId(request.getClientSessionId());
        completion.setUpdatedAt(new Date());
        completionRepository.save(completion);
        session.setScore(totalScore); session.setMaxScore(totalMax); session.setAccuracy(accuracy);
        session.setStarsEarned(deltaStars); session.setPointsEarned(deltaPoints);
        session = sessionRepository.save(session);
        return response(session, profile, completion);
    }

    @Override @Transactional(readOnly = true)
    public LessonAssessmentResponse getDetail(Long sessionId, User learner) {
        LessonAssessmentSession session = sessionRepository.findByIdAndLearnerId(sessionId, learner.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return response(session, profile(learner), completionRepository.findByLessonIdAndLearnerId(session.getLesson().getId(), learner.getId()).orElse(null));
    }

    private LearnerProfile profile(User learner) { return learnerProfileRepository.findByUserId(learner.getId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); }
    private int configInt(String key, int fallback) { return appConfigRepository.findByConfigKey(key).map(v -> { try { return Integer.parseInt(v.getConfigValue()); } catch (Exception e) { return fallback; }}).orElse(fallback); }
    private int starsFor(BigDecimal accuracy) { double v = accuracy.doubleValue(); return v >= 90 ? 3 : v >= 70 ? 2 : v >= 50 ? 1 : 0; }
    private boolean answersMatch(Quiz quiz, String selected) {
        String correct = quiz.getCorrectAnswer() == null ? "" : quiz.getCorrectAnswer();
        if (normalize(selected).equals(normalize(correct))) return true;
        if (correct.trim().matches("(?i)[A-D]")) {
            try { List<?> options = new com.fasterxml.jackson.databind.ObjectMapper().readValue(quiz.getOptions(), List.class); int index = Character.toUpperCase(correct.trim().charAt(0)) - 'A'; return index >= 0 && index < options.size() && normalize(selected).equals(normalize(String.valueOf(options.get(index)))); } catch (Exception ignored) { }
        }
        return false;
    }
    private String normalize(String value) { return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replaceAll("[^\\p{Alnum}]", "").toLowerCase(Locale.ROOT); }
    private <T> void validateCompleteSet(Set<Long> expected, List<T> submitted, java.util.function.Function<T, Long> id, String label) {
        Set<Long> actual = submitted.stream().map(id).collect(Collectors.toSet());
        if (actual.size() != submitted.size() || !actual.equals(expected)) throw new AppException(ErrorCode.BAD_REQUEST, "Phải nộp đầy đủ và không trùng " + label + " của bài học");
    }
    private LessonAssessmentResponse response(LessonAssessmentSession s, LearnerProfile p, LessonCompletion c) {
        return LessonAssessmentResponse.builder().id(s.getId()).lessonId(s.getLesson().getId()).clientSessionId(s.getClientSessionId()).status(s.getStatus()).score(s.getScore()).maxScore(s.getMaxScore()).accuracy(s.getAccuracy()).starsEarned(s.getStarsEarned()).pointsEarned(s.getPointsEarned()).completed(c != null && Boolean.TRUE.equals(c.getCompleted())).lessonStars(c == null ? 0 : c.getStars()).totalStars(p.getTotalStars()).totalPoints(p.getTotalPoints()).completedAt(s.getCompletedAt())
                .quizAnswers(s.getQuizAnswers().stream().map(a -> LessonAssessmentResponse.QuizAnswer.builder().quizId(a.getQuiz().getId()).selectedAnswer(a.getSelectedAnswer()).correct(a.getCorrect()).score(a.getScore()).maxScore(a.getMaxScore()).build()).toList())
                .minigameResults(s.getMinigameResults().stream().map(a -> LessonAssessmentResponse.MinigameResult.builder().challengeId(a.getMinigame().getId()).score(a.getScore().intValue()).maxScore(a.getMaxScore().intValue()).build()).toList()).build();
    }
}
