package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.QuizAttemptRequest;
import com.example.vietstage_web_be.dto.response.QuizAttemptResponse;
import com.example.vietstage_web_be.entity.LearnerProfile;
import com.example.vietstage_web_be.entity.Lesson;
import com.example.vietstage_web_be.entity.Quiz;
import com.example.vietstage_web_be.entity.QuizAttempt;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.repository.AppConfigRepository;
import com.example.vietstage_web_be.repository.LearnerProfileRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.QuizAttemptRepository;
import com.example.vietstage_web_be.repository.QuizRepository;
import com.example.vietstage_web_be.service.ILeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private ILeaderboardService leaderboardService;
    @Mock
    private AppConfigRepository appConfigRepository;
    @Mock
    private LearnerProfileRepository learnerProfileRepository;

    @InjectMocks
    private QuizServiceImpl quizService;

    private User learner;
    private LearnerProfile profile;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        learner = User.builder().id(7L).email("learner@example.com").fullName("Learner").build();
        profile = LearnerProfile.builder()
                .userId(7L)
                .user(learner)
                .totalPoints(0)
                .totalStars(1)
                .spendableStars(1)
                .build();
        Lesson lesson = Lesson.builder().id(11L).title("Nhạc lý cơ bản").build();
        quiz = Quiz.builder()
                .id(3L)
                .lesson(lesson)
                .title("Nhận diện nốt Đô")
                .questionType("GENERAL")
                .question("Đây là nốt gì?")
                .options("[\"Đô\", \"Rê\", \"Mi\", \"Fa\"]")
                .correctAnswer("Đô")
                .build();

        when(quizRepository.findById(3L)).thenReturn(Optional.of(quiz));
        when(quizAttemptRepository.findByClientAttemptIdAndLearnerId(anyString(), any(Long.class)))
                .thenReturn(Optional.empty());
        when(learnerProfileRepository.findByUserId(7L)).thenReturn(Optional.of(profile));
        when(appConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(quizAttemptRepository.save(any(QuizAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void gradesVisibleAnswerAndStoresTrimmedAttemptWithConfiguredRewards() {
        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setSelectedAnswer("  đô  ");
        request.setClientAttemptId("quiz-attempt-1");

        QuizAttemptResponse response = quizService.submitAttempt(3L, request, learner);

        assertThat(response.getIsCorrect()).isTrue();
        assertThat(response.getScore()).isEqualByComparingTo("100.0");
        assertThat(response.getPointsEarned()).isEqualTo(10);
        assertThat(response.getStarsEarned()).isEqualTo(2);
        assertThat(profile.getTotalStars()).isEqualTo(3);
        assertThat(profile.getSpendableStars()).isEqualTo(3);
        verify(leaderboardService).addPoints(learner, 10, "QUIZ");

        ArgumentCaptor<QuizAttempt> captor = ArgumentCaptor.forClass(QuizAttempt.class);
        verify(quizAttemptRepository).save(captor.capture());
        assertThat(captor.getValue().getSelectedAnswer()).isEqualTo("đô");
    }

    @Test
    void acceptsLetterStoredAsCorrectAnswerAndDoesNotRewardWrongAnswer() {
        quiz.setCorrectAnswer("A");
        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setSelectedAnswer("Đô");
        request.setClientAttemptId("quiz-attempt-2-correct");

        QuizAttemptResponse correct = quizService.submitAttempt(3L, request, learner);
        assertThat(correct.getIsCorrect()).isTrue();
        assertThat(correct.getPointsEarned()).isEqualTo(10);
        assertThat(profile.getTotalStars()).isEqualTo(3);

        request.setSelectedAnswer("Rê");
        request.setClientAttemptId("quiz-attempt-2-wrong");
        QuizAttemptResponse wrong = quizService.submitAttempt(3L, request, learner);

        assertThat(wrong.getIsCorrect()).isFalse();
        assertThat(wrong.getScore()).isEqualByComparingTo("0");
        assertThat(wrong.getPointsEarned()).isZero();
        assertThat(wrong.getStarsEarned()).isZero();
        assertThat(profile.getTotalStars()).isEqualTo(3);
        assertThat(profile.getSpendableStars()).isEqualTo(3);
    }
}
