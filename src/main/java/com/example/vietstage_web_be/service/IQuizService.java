package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.LearnerQuizRequest;
import com.example.vietstage_web_be.dto.request.QuizAttemptRequest;
import com.example.vietstage_web_be.dto.request.QuizRequest;
import com.example.vietstage_web_be.dto.response.LearnerQuizProgressResponse;
import com.example.vietstage_web_be.dto.response.QuizAttemptResponse;
import com.example.vietstage_web_be.dto.response.QuizResponse;
import com.example.vietstage_web_be.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IQuizService {
    List<QuizResponse> getQuizzesByLesson(Long lessonId, User currentUser);
    QuizResponse createQuiz(Long lessonId, QuizRequest request);
    QuizResponse updateQuiz(Long id, QuizRequest request);
    void deleteQuiz(Long id);
    
    QuizAttemptResponse submitAttempt(Long quizId, QuizAttemptRequest request, User learner);
    Page<QuizAttemptResponse> getAttempts(Long quizId, Pageable pageable, User learner);

    /*Instructor*/
    LearnerQuizProgressResponse createQuizByLearnerLever(LearnerQuizRequest request);

    LearnerQuizProgressResponse updateQuizByLearnerLevel(Long quizId, LearnerQuizRequest request);

    List<LearnerQuizProgressResponse> getQuizzesByLearnerLevel(Long learnerId, Long instrumentId);

    /*Learner*/
    List<LearnerQuizProgressResponse> getQuizzes(Long learnerId, Long instrumentId, Long lessonId
    );
}