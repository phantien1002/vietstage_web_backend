package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.QuizAttemptRequest;
import com.example.vietstage_web_be.dto.request.QuizRequest;
import com.example.vietstage_web_be.dto.response.QuizAttemptResponse;
import com.example.vietstage_web_be.dto.response.QuizResponse;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.Quiz;
import com.example.vietstage_web_be.entity.QuizAttempt;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.AppConfigRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.LearnerProfileRepository;
import com.example.vietstage_web_be.repository.QuizAttemptRepository;
import com.example.vietstage_web_be.repository.QuizRepository;
import com.example.vietstage_web_be.service.IQuizService;
import com.example.vietstage_web_be.service.ILeaderboardService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements IQuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final LessonRepository lessonRepository;
    private final ILeaderboardService leaderboardService;
    private final AppConfigRepository appConfigRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<QuizResponse> getQuizzesByLesson(Long lessonId, User currentUser) {
        List<Quiz> quizzes = quizRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        
        return quizzes.stream().map(quiz -> {
            QuizResponse.QuizResponseBuilder builder = QuizResponse.builder()
                    .id(quiz.getId())
                    .title(quiz.getTitle())
                    .questionType(quiz.getQuestionType())
                    .note(quiz.getNote())
                    .audioUrl(quiz.getAudioUrl())
                    .question(quiz.getQuestion())
                    .options(quiz.getOptions())
                    .orderIndex(quiz.getOrderIndex())
                    // Answers are authoring data. A learner receives it only after submitting.
                    .correctAnswer(canViewCorrectAnswer(currentUser) ? quiz.getCorrectAnswer() : null);
            
            return builder.build();
        }).collect(Collectors.toList());
    }

    @Override
    public QuizResponse createQuiz(Long lessonId, QuizRequest request) {
        validateQuizRequest(request);
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        Quiz quiz = Quiz.builder()
                .lesson(lesson)
                .title(request.getTitle())
                .questionType(request.getQuestionType())
                .note(request.getNote())
                .audioUrl(request.getAudioUrl())
                .question(request.getQuestion())
                .options(request.getOptions())
                .correctAnswer(request.getCorrectAnswer())
                .orderIndex(request.getOrderIndex())
                .createdAt(LocalDateTime.now())
                .build();

        quiz = quizRepository.save(quiz);

        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .questionType(quiz.getQuestionType())
                .note(quiz.getNote())
                .audioUrl(quiz.getAudioUrl())
                .question(quiz.getQuestion())
                .options(quiz.getOptions())
                .correctAnswer(quiz.getCorrectAnswer())
                .orderIndex(quiz.getOrderIndex())
                .build();
    }

    @Override
    public QuizResponse updateQuiz(Long id, QuizRequest request) {
        validateQuizRequest(request);
        
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND)); 

        quiz.setTitle(request.getTitle());
        quiz.setQuestionType(request.getQuestionType());
        quiz.setNote(request.getNote());
        quiz.setAudioUrl(request.getAudioUrl());
        quiz.setQuestion(request.getQuestion());
        quiz.setOptions(request.getOptions());
        quiz.setCorrectAnswer(request.getCorrectAnswer());
        quiz.setOrderIndex(request.getOrderIndex());

        quiz = quizRepository.save(quiz);

        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .questionType(quiz.getQuestionType())
                .note(quiz.getNote())
                .audioUrl(quiz.getAudioUrl())
                .question(quiz.getQuestion())
                .options(quiz.getOptions())
                .correctAnswer(quiz.getCorrectAnswer())
                .orderIndex(quiz.getOrderIndex())
                .build();
    }

    @Override
    public void deleteQuiz(Long id) {
        if (!quizRepository.existsById(id)) {
            throw new AppException(ErrorCode.QUIZ_NOT_FOUND);
        }
        quizRepository.deleteById(id);
    }

    @Override
    @Transactional
    public QuizAttemptResponse submitAttempt(Long quizId, QuizAttemptRequest request, User learner) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        com.example.vietstage_web_be.entity.LearnerProfile profile = learnerProfileRepository.findByUserId(learner.getId()).orElse(null);

        int configuredPoints = positiveRewardConfig("scoring.quiz.points", 10);
        int configuredStars = positiveRewardConfig("scoring.quiz.stars", 2);

        if (request.getClientAttemptId() != null) {
            java.util.Optional<QuizAttempt> existingAttempt = quizAttemptRepository
                    .findByClientAttemptIdAndLearnerId(request.getClientAttemptId(), learner.getId());
            if (existingAttempt.isPresent()) {
                QuizAttempt attempt = existingAttempt.get();
                if (!attempt.getQuiz().getId().equals(quiz.getId())) {
                    throw new AppException(ErrorCode.BAD_REQUEST, "Mã attempt đã được dùng cho một câu hỏi khác");
                }
                return QuizAttemptResponse.builder()
                        .id(attempt.getId())
                        .quizId(quiz.getId())
                        .learnerId(learner.getId())
                        .selectedAnswer(attempt.getSelectedAnswer())
                        .isCorrect(attempt.getIsCorrect())
                        .score(attempt.getScore())
                        .pointsEarned(attempt.getPointsEarned() != null ? attempt.getPointsEarned() : (attempt.getIsCorrect() ? 10 : 0))
                        .starsEarned(attempt.getStarsEarned() != null ? attempt.getStarsEarned() : 0)
                        .totalStars(profile != null ? profile.getTotalStars() : 0)
                        .spendableStars(profile != null ? profile.getSpendableStars() : 0)
                        .totalPoints(profile != null ? profile.getTotalPoints() : 0)
                        .attemptedAt(attempt.getAttemptedAt())
                        .correctAnswer(quiz.getCorrectAnswer())
                        .build();
            }
        }

        String selectedAnswer = request.getSelectedAnswer() != null ? request.getSelectedAnswer().trim() : "";
        String correctAnswer = quiz.getCorrectAnswer() != null ? quiz.getCorrectAnswer().trim() : "";
        
        boolean isCorrect = correctAnswer.equals(selectedAnswer);
        BigDecimal score = isCorrect ? BigDecimal.valueOf(100.0) : BigDecimal.ZERO;
        Integer pointsEarned = isCorrect ? configuredPoints : 0;
        Integer starsEarned = isCorrect ? configuredStars : 0;

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .learner(learner)
                .selectedAnswer(request.getSelectedAnswer())
                .isCorrect(isCorrect)
                .score(score)
                .pointsEarned(pointsEarned)
                .starsEarned(starsEarned)
                .attemptedAt(LocalDateTime.now())
                .clientAttemptId(request.getClientAttemptId())
                .build();

        attempt = quizAttemptRepository.save(attempt);
        
        if (pointsEarned > 0) {
            leaderboardService.addPoints(learner, pointsEarned, "QUIZ");
        }
        
        if (profile != null && starsEarned > 0) {
            profile.setTotalStars(profile.getTotalStars() + starsEarned);
            profile.setSpendableStars(profile.getSpendableStars() + starsEarned);
            learnerProfileRepository.save(profile);
        }

        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .quizId(quiz.getId())
                .learnerId(learner.getId())
                .selectedAnswer(attempt.getSelectedAnswer())
                .isCorrect(attempt.getIsCorrect())
                .score(attempt.getScore())
                .pointsEarned(pointsEarned)
                .starsEarned(starsEarned)
                .totalStars(profile != null ? profile.getTotalStars() : 0)
                .spendableStars(profile != null ? profile.getSpendableStars() : 0)
                .totalPoints(profile != null ? profile.getTotalPoints() : 0)
                .attemptedAt(attempt.getAttemptedAt())
                .correctAnswer(quiz.getCorrectAnswer())
                .build();
    }

    @Override
    public Page<QuizAttemptResponse> getAttempts(Long quizId, Pageable pageable, User learner) {
        Page<QuizAttempt> attempts = quizAttemptRepository.findByQuizIdAndLearnerIdOrderByAttemptedAtDesc(quizId, learner.getId(), pageable);
        
        return attempts.map(attempt -> QuizAttemptResponse.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .learnerId(attempt.getLearner().getId())
                .selectedAnswer(attempt.getSelectedAnswer())
                .isCorrect(attempt.getIsCorrect())
                .score(attempt.getScore())
                // Assuming pointsEarned isn't stored in entity right now, we calculate or retrieve it,
                // but let's just return what is available or a mock for now.
                // For a proper solution, pointsEarned might need to be stored on QuizAttempt or joined from point_transactions.
                // We'll set it to 0 for historical fetches unless added to DB.
                .pointsEarned(attempt.getPointsEarned() != null ? attempt.getPointsEarned() : (attempt.getIsCorrect() ? 10 : 0))
                .starsEarned(attempt.getStarsEarned() != null ? attempt.getStarsEarned() : 0)
                .attemptedAt(attempt.getAttemptedAt())
                .build());
    }

    /**
     * A correct quiz answer must not be recorded as a successful reward with
     * zero XP or zero stars. Older installations may contain a zero/invalid
     * config value, so use the product default until an admin corrects it.
     */
    private int positiveRewardConfig(String key, int fallback) {
        try {
            return appConfigRepository.findByConfigKey(key)
                    .map(config -> (int) Math.round(Double.parseDouble(config.getConfigValue())))
                    .filter(value -> value > 0)
                    .orElse(fallback);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private boolean canViewCorrectAnswer(User user) {
        if (user == null || user.getRole() == null || user.getRole().getName() == null) return false;
        String role = user.getRole().getName();
        return "INSTRUCTOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }
    
    private void validateQuizRequest(QuizRequest request) {
        if ("NOTE_IDENTIFICATION".equals(request.getQuestionType())) {
            if (request.getNote() == null || request.getNote().trim().isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST); // TODO: You might want a specific error code for missing note
            }
        }

        try {
            List<String> options = objectMapper.readValue(request.getOptions(), new TypeReference<List<String>>() {});
            if (options == null || options.size() < 4) {
                throw new AppException(ErrorCode.BAD_REQUEST); // Options must have at least 4 items
            }
            if (!options.contains(request.getCorrectAnswer())) {
                throw new AppException(ErrorCode.BAD_REQUEST); // Correct answer must be exactly one of the options
            }
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Options must be a valid JSON string array
        }
    }
}
