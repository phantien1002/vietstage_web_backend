package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.*;
import com.example.vietstage_web_be.dto.response.LessonAssessmentResponse;
import com.example.vietstage_web_be.entity.*;
import com.example.vietstage_web_be.repository.*;
import com.example.vietstage_web_be.service.ILeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LessonAssessmentServiceImplTest {
    @Mock private LessonRepository lessonRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private MinigameChallengeRepository minigameChallengeRepository;
    @Mock private LessonAssessmentSessionRepository sessionRepository;
    @Mock private LessonCompletionRepository completionRepository;
    @Mock private LearnerProfileRepository learnerProfileRepository;
    @Mock private AppConfigRepository appConfigRepository;
    @Mock private ILeaderboardService leaderboardService;
    @InjectMocks private LessonAssessmentServiceImpl service;

    private User learner;
    private Lesson lesson;

    @BeforeEach void setUp() {
        learner = User.builder().id(7L).build();
        lesson = Lesson.builder().id(11L).status("PUBLISHED").title("Lesson").build();
        LearnerProfile profile = LearnerProfile.builder().userId(7L).totalPoints(0).totalStars(0).spendableStars(0).build();
        when(lessonRepository.findById(11L)).thenReturn(Optional.of(lesson));
        when(learnerProfileRepository.findByUserId(7L)).thenReturn(Optional.of(profile));
        when(completionRepository.findByLessonIdAndLearnerId(11L, 7L)).thenReturn(Optional.empty());
        when(appConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(sessionRepository.findByLearnerIdAndClientSessionId(eq(7L), anyString())).thenReturn(Optional.empty());
        when(sessionRepository.save(any(LessonAssessmentSession.class))).thenAnswer(i -> { LessonAssessmentSession s = i.getArgument(0); s.setId(99L); return s; });
        when(completionRepository.save(any(LessonCompletion.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test void gradesServerSideAndAwardsOnlyImprovement() {
        Quiz quiz = Quiz.builder().id(1L).lesson(lesson).options("[\"Do\",\"Re\"]").correctAnswer("A").build();
        MinigameChallenge game = MinigameChallenge.builder().id(2L).lesson(lesson).maxScore(100).build();
        when(quizRepository.findByLessonIdOrderByOrderIndexAsc(11L)).thenReturn(List.of(quiz));
        when(minigameChallengeRepository.findByLessonIdOrderByOrderIndexAsc(11L)).thenReturn(List.of(game));
        LessonAssessmentRequest request = request("session-1", "Do", 50);

        LessonAssessmentResponse response = service.submit(11L, request, learner);

        assertThat(response.getScore()).isEqualByComparingTo("150");
        assertThat(response.getAccuracy()).isEqualByComparingTo("75");
        assertThat(response.getStarsEarned()).isEqualTo(2);
        assertThat(response.getPointsEarned()).isEqualTo(20);
        assertThat(response.getCompleted()).isTrue();
        verify(leaderboardService).addPoints(learner, 20, "LESSON_ASSESSMENT");
    }

    @Test void rejectsPartialOrForeignContent() {
        when(quizRepository.findByLessonIdOrderByOrderIndexAsc(11L)).thenReturn(List.of(Quiz.builder().id(1L).lesson(lesson).build()));
        when(minigameChallengeRepository.findByLessonIdOrderByOrderIndexAsc(11L)).thenReturn(List.of());
        LessonAssessmentRequest request = request("session-2", "Do", 0);
        request.setMinigameResults(List.of());
        request.setQuizAnswers(List.of());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.submit(11L, request, learner)).isInstanceOf(RuntimeException.class);
        verify(sessionRepository, never()).save(any());
    }

    private LessonAssessmentRequest request(String id, String answer, int score) {
        AssessmentQuizAnswerRequest quiz = new AssessmentQuizAnswerRequest(); quiz.setQuizId(1L); quiz.setSelectedAnswer(answer);
        AssessmentMinigameResultRequest game = new AssessmentMinigameResultRequest(); game.setChallengeId(2L); game.setScore(score); game.setStartedAt(LocalDateTime.now()); game.setCompletedAt(LocalDateTime.now());
        LessonAssessmentRequest request = new LessonAssessmentRequest(); request.setClientSessionId(id); request.setStartedAt(LocalDateTime.now()); request.setQuizAnswers(List.of(quiz)); request.setMinigameResults(List.of(game)); return request;
    }
}
