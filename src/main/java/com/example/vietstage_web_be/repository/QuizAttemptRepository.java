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

    java.util.Optional<QuizAttempt> findByClientAttemptId(String clientAttemptId);
}

