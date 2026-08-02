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
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.QuizAttemptRepository;
import com.example.vietstage_web_be.repository.QuizRepository;
import com.example.vietstage_web_be.service.IQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    @Override
    public List<QuizResponse> getQuizzesByLesson(Long lessonId, User currentUser) {
        List<Quiz> quizzes = quizRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        
        return quizzes.stream().map(quiz -> {
            QuizResponse.QuizResponseBuilder builder = QuizResponse.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .options(quiz.getOptions())
                    .orderIndex(quiz.getOrderIndex());
                    
            // Only return correctAnswer if the user is INSTRUCTOR or ADMIN
            if (currentUser != null && (currentUser.getRole().getName().equals("INSTRUCTOR") || currentUser.getRole().getName().equals("ADMIN"))) {
                builder.correctAnswer(quiz.getCorrectAnswer());
            }
            
            return builder.build();
        }).collect(Collectors.toList());
    }

    @Override
    public QuizResponse createQuiz(Long lessonId, QuizRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        Quiz quiz = Quiz.builder()
                .lesson(lesson)
                .question(request.getQuestion())
                .options(request.getOptions())
                .correctAnswer(request.getCorrectAnswer())
                .orderIndex(request.getOrderIndex())
                .createdAt(LocalDateTime.now())
                .build();

        quiz = quizRepository.save(quiz);

        return QuizResponse.builder()
                .id(quiz.getId())
                .question(quiz.getQuestion())
                .options(quiz.getOptions())
                .correctAnswer(quiz.getCorrectAnswer())
                .orderIndex(quiz.getOrderIndex())
                .build();
    }

    @Override
    public QuizResponse updateQuiz(Long id, QuizRequest request) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND)); 

        quiz.setQuestion(request.getQuestion());
        quiz.setOptions(request.getOptions());
        quiz.setCorrectAnswer(request.getCorrectAnswer());
        quiz.setOrderIndex(request.getOrderIndex());

        quiz = quizRepository.save(quiz);

        return QuizResponse.builder()
                .id(quiz.getId())
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
    public QuizAttemptResponse submitAttempt(Long quizId, QuizAttemptRequest request, User learner) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        boolean isCorrect = quiz.getCorrectAnswer().equals(request.getSelectedAnswer());
        BigDecimal score = isCorrect ? BigDecimal.valueOf(100.0) : BigDecimal.ZERO;
        Integer pointsEarned = isCorrect ? 10 : 0; // Configurable logic can be added later

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .learner(learner)
                .selectedAnswer(request.getSelectedAnswer())
                .isCorrect(isCorrect)
                .score(score)
                .attemptedAt(LocalDateTime.now())
                .build();

        attempt = quizAttemptRepository.save(attempt);

        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .quizId(quiz.getId())
                .learnerId(learner.getId())
                .selectedAnswer(attempt.getSelectedAnswer())
                .isCorrect(attempt.getIsCorrect())
                .score(attempt.getScore())
                .pointsEarned(pointsEarned)
                .attemptedAt(attempt.getAttemptedAt())
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
                .pointsEarned(attempt.getIsCorrect() ? 10 : 0)
                .attemptedAt(attempt.getAttemptedAt())
                .build());
    }
}
