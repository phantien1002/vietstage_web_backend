package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.LearnerQuizRequest;
import com.example.vietstage_web_be.dto.request.QuizAttemptRequest;
import com.example.vietstage_web_be.dto.request.QuizRequest;
import com.example.vietstage_web_be.dto.response.LearnerProgressItemResponse;
import com.example.vietstage_web_be.dto.response.LearnerQuizProgressResponse;
import com.example.vietstage_web_be.dto.response.QuizAttemptResponse;
import com.example.vietstage_web_be.dto.response.QuizResponse;
import com.example.vietstage_web_be.entity.*;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.*;
import com.example.vietstage_web_be.service.ILearnerProgressService;
import com.example.vietstage_web_be.service.IQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements IQuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final ILearnerProgressService learnerProgressService;

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

    @Override
    public LearnerQuizProgressResponse createQuizByLearnerLever(LearnerQuizRequest request) {
        User learner = userRepository.findById(request.getLearnerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getInstrument() == null
            || lesson.getInstrument().getId() == null
            || !lesson.getInstrument().getId().equals(request.getInstrumentId())) {
            throw new AppException(ErrorCode.INSTRUMENT_NOT_FOUND);
        }

        SkillLevel learnerCurrentLevel = getCurrentLearnerLevel(request.getLearnerId(), request.getInstrumentId());

        if (lesson.getSkillLevel() == null || !lesson.getSkillLevel().getId().equals(learnerCurrentLevel.getId())) {
            throw new AppException(ErrorCode.SKILL_LEVEL_NOT_FOUND);
        }

        Quiz quiz = new Quiz();
        quiz.setLesson(lesson);
        quiz.setQuestion(request.getQuestion());
        quiz.setOptions(request.getOptions());
        quiz.setCorrectAnswer(request.getCorrectAnswer());
        quiz.setOrderIndex(request.getOrderIndex());
        quiz.setCreatedAt(LocalDateTime.now());

        Quiz savedQuiz = quizRepository.save(quiz);

        return LearnerQuizProgressResponse.builder()
                .id(savedQuiz.getId())
                .lessonId(savedQuiz.getLesson().getId())
                .lessonCode(savedQuiz.getLesson().getLessonCode())
                .skillLevelId(savedQuiz.getLesson().getSkillLevel().getId())
                .levelCode(savedQuiz.getLesson().getSkillLevel().getLevelCode())
                .levelOrderIndex(savedQuiz.getLesson().getSkillLevel().getOrderIndex())
                .question(savedQuiz.getQuestion())
                .options(savedQuiz.getOptions())
                .correctAnswer(savedQuiz.getCorrectAnswer())
                .orderIndex(savedQuiz.getOrderIndex())
                .createAt(savedQuiz.getCreatedAt())
                .build();
    }

    @Override
    public LearnerQuizProgressResponse updateQuizByLearnerLevel(Long quizId, LearnerQuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        User learner = userRepository.findById(request.getLearnerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getInstrument() == null
            || lesson.getInstrument().getId() == null
            || !lesson.getInstrument().getId().equals(request.getInstrumentId())) {
            throw new AppException(ErrorCode.INSTRUMENT_NOT_FOUND);
        }

        SkillLevel learnerCurrentLevel = getCurrentLearnerLevel(request.getLearnerId(), request.getInstrumentId());

        if (lesson.getSkillLevel() == null
            || !lesson.getSkillLevel().getId().equals(learnerCurrentLevel.getId())) {
            throw new AppException(ErrorCode.SKILL_LEVEL_NOT_FOUND);
        }

        quiz.setLesson(lesson);
        quiz.setQuestion(request.getQuestion());
        quiz.setOptions(request.getOptions());
        quiz.setCorrectAnswer(request.getCorrectAnswer());
        quiz.setOrderIndex(request.getOrderIndex());

        quiz = quizRepository.save(quiz);

        return LearnerQuizProgressResponse.builder()
                .id(quiz.getId())
                .lessonId(quiz.getLesson().getId())
                .lessonCode(quiz.getLesson().getLessonCode())
                .skillLevelId(quiz.getLesson().getSkillLevel().getId())
                .levelCode(quiz.getLesson().getSkillLevel().getLevelCode())
                .levelOrderIndex(quiz.getLesson().getSkillLevel().getOrderIndex())
                .question(quiz.getQuestion())
                .options(quiz.getOptions())
                .correctAnswer(quiz.getCorrectAnswer())
                .orderIndex(quiz.getOrderIndex())
                .createAt(quiz.getCreatedAt())
                .updateAt(LocalDateTime.now())
                .build();
    }

    @Override
    public List<LearnerQuizProgressResponse> getQuizzesByLearnerLevel(Long learnerId, Long instrumentId) {
        SkillLevel currentLevel = getCurrentLearnerLevel(learnerId, instrumentId);

        List<Quiz> quizList = quizRepository.findByInstrumentIdAndSkillLevelId(instrumentId, currentLevel.getId());

        List<LearnerQuizProgressResponse> responseList = new ArrayList<>();

        for (Quiz quiz : quizList) {
            Lesson lesson = quiz.getLesson();

            responseList.add(
                    LearnerQuizProgressResponse.builder()
                            .id(quiz.getId())
                            .lessonId(lesson.getId())
                            .levelCode(lesson.getLessonCode())
                            .instrumentId(lesson.getInstrument().getId())
                            .instrumentCode(lesson.getInstrument().getInstrumentCode())
                            .skillLevelId(lesson.getSkillLevel().getId())
                            .lessonCode(lesson.getSkillLevel().getLevelCode())
                            .levelOrderIndex(lesson.getSkillLevel().getOrderIndex())
                            .question(quiz.getQuestion())
                            .options(quiz.getOptions())
                            .correctAnswer(quiz.getCorrectAnswer())
                            .orderIndex(quiz.getOrderIndex())
                            .build()
            );
        }

        return responseList;
    }

    @Override
    public List<LearnerQuizProgressResponse> getQuizzes(Long learnerId, Long instrumentId, Long lessonId) {
        List<Quiz> quizzes;

        if (learnerId != null) {
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

            if (lesson.getInstrument() == null
                || !lesson.getInstrument().getId().equals(instrumentId)) {
                throw new AppException(ErrorCode.INSTRUMENT_NOT_FOUND);
            }
            quizzes = quizRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        }else {
            SkillLevel currentLevel = getCurrentLearnerLevel(learnerId, instrumentId);

            quizzes = quizRepository.findByInstrumentIdAndSkillLevelId(instrumentId, currentLevel.getId());
        }

        List<LearnerQuizProgressResponse> responseList = new ArrayList<>();

        for (Quiz quiz : quizzes) {
            Lesson lesson = quiz.getLesson();

            Optional<QuizAttempt> attempt = quizAttemptRepository.findTopByQuizIdAndLearnerIdOrderByAttemptedAtDesc(quiz.getId(), learnerId);

            if (attempt.isPresent()) {
                QuizAttempt quizAttempt = attempt.get();

                responseList.add(
                        LearnerQuizProgressResponse.builder()
                                .id(quiz.getId())
                                .lessonId(lesson.getId())
                                .lessonCode(lesson.getLessonCode())
                                .instrumentId(lesson.getInstrument().getId())
                                .instrumentCode(lesson.getInstrument().getInstrumentCode())
                                .skillLevelId(lesson.getSkillLevel().getId())
                                .levelCode(lesson.getSkillLevel().getLevelCode())
                                .levelOrderIndex(lesson.getSkillLevel().getOrderIndex())
                                .question(quiz.getQuestion())
                                .options(quiz.getOptions())
                                .correctAnswer(quiz.getCorrectAnswer())
                                .orderIndex(quiz.getOrderIndex())
                                .attempted(true)
                                .selectedAnswer(quizAttempt.getSelectedAnswer())
                                .correct(quizAttempt.getIsCorrect())
                                .score(quizAttempt.getScore())
                                .attemptedAt(quizAttempt.getAttemptedAt())
                                .build()
                );
            }else {
                responseList.add(
                        LearnerQuizProgressResponse.builder()
                                .id(quiz.getId())
                                .lessonId(lesson.getId())
                                .lessonCode(lesson.getLessonCode())
                                .instrumentId(lesson.getInstrument().getId())
                                .instrumentCode(lesson.getInstrument().getInstrumentCode())
                                .skillLevelId(lesson.getSkillLevel().getId())
                                .levelCode(lesson.getSkillLevel().getLevelCode())
                                .levelOrderIndex(lesson.getSkillLevel().getOrderIndex())
                                .question(quiz.getQuestion())
                                .options(quiz.getOptions())
                                .correctAnswer(quiz.getCorrectAnswer())
                                .orderIndex(quiz.getOrderIndex())
                                .attempted(false)
                                .selectedAnswer(null)
                                .correct(null)
                                .score(null)
                                .attemptedAt(null)
                                .build()
                );
            }
        }

        return responseList;
    }

    private SkillLevel getCurrentLearnerLevel(Long learnerId, Long instrumentId) {
        List<Lesson> lessons = lessonRepository.findByInstrumentId(instrumentId);

        if (lessons.isEmpty()){
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }

        Map<Long, List<Lesson>> lessonsByLevel = new LinkedHashMap<>();

        for (Lesson lesson : lessons){
            if (lesson.getSkillLevel() == null) continue;

            lessonsByLevel.computeIfAbsent(lesson.getSkillLevel().getId(), k -> new ArrayList<>()).add(lesson);
        }

        List<List<Lesson>> levels = new  ArrayList<>(lessonsByLevel.values());

        levels.sort(Comparator.comparing(levelLessons -> levelLessons.get(0).getSkillLevel().getOrderIndex()));

        SkillLevel currentLevel = levels.get(0).get(0).getSkillLevel();

        for (List<Lesson> levelLessons : levels){
            SkillLevel level = levelLessons.get(0).getSkillLevel();

            List<LearnerProgressItemResponse> progressList = learnerProgressService.getLearnerProgress(learnerId, instrumentId, level.getId());

            long totalLessons = levelLessons.size();

            long completedLessons = progressList.stream().filter(
                    item -> Boolean.TRUE.equals(item.getCompleted())
            ).count();

            if (totalLessons > 0 && completedLessons == totalLessons){
                currentLevel = level;
            }else {
                break;
            }
        }

        for (int i = 0; i < levels.size(); i++) {
            SkillLevel level = levels.get(i).get(0).getSkillLevel();

            if (level.getId().equals(currentLevel.getId())){
                if (i + 1 < levels.size()){
                    List<LearnerProgressItemResponse> progressList = learnerProgressService.getLearnerProgress(learnerId, instrumentId, level.getId());

                    long completed = progressList.stream().filter(
                            item -> Boolean.TRUE.equals(item.getCompleted())
                    ).count();

                    if (completed == levels.get(i).size()){
                        currentLevel = levels.get(i + 1).get(0).getSkillLevel();
                    }
                }
                break;
            }
        }
        return currentLevel;
    }
}
