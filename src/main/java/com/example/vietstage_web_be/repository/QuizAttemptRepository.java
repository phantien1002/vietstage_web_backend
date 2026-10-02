package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    @Query("SELECT COUNT(qa) FROM QuizAttempt qa " +
            "JOIN qa.quiz q " +
            "WHERE q.lesson.id = :lessonId AND qa.learner.id = :learnerId")
    Integer countQuizAttemptsByLessonAndLearner(@Param("lessonId") Long lessonId, @Param("learnerId") Long learnerId);

    org.springframework.data.domain.Page<QuizAttempt> findByQuizIdAndLearnerIdOrderByAttemptedAtDesc(Long quizId, Long learnerId, org.springframework.data.domain.Pageable pageable);

    java.util.Optional<QuizAttempt> findByClientAttemptIdAndLearnerId(String clientAttemptId, Long learnerId);

    org.springframework.data.domain.Page<QuizAttempt> findByLearnerId(Long learnerId, org.springframework.data.domain.Pageable pageable);

    java.util.Optional<QuizAttempt> findByIdAndLearnerId(Long id, Long learnerId);

    @Query("SELECT COUNT(DISTINCT qa.learner.id) as learnersCount, COUNT(qa) as totalAttempts, " +
           "AVG(qa.score) as averageScore, " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(qa), 0) as passRate " +
           "FROM QuizAttempt qa WHERE qa.quiz.id = :quizId")
    java.util.Map<String, Object> getQuizStatistics(@Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(DISTINCT qa.learner.id) as learnersCount, COUNT(qa) as totalAttempts, " +
           "AVG(qa.score) as averageScore, " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(qa), 0) as passRate " +
           "FROM QuizAttempt qa WHERE qa.quiz.lesson.id = :lessonId")
    java.util.Map<String, Object> getQuizStatisticsByLesson(@Param("lessonId") Long lessonId);
}

