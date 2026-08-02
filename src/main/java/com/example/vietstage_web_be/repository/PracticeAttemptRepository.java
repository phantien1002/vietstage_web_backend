package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.PracticeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticeAttemptRepository extends JpaRepository<PracticeAttempt, Long> {
    @Query("SELECT COUNT(pa) FROM PracticeAttempt pa " +
            "JOIN pa.exercise e " +
            "WHERE e.lesson.id = :lessonId AND pa.learner.id = :learnerId")
    Integer countAttemptsByLessonAndLearner(@Param("lessonId") Long lessonId, @Param("learnerId") Long learnerId);

    @Query("SELECT MAX(pa.totalScore) FROM PracticeAttempt pa " +
            "JOIN pa.exercise e " +
            "WHERE e.lesson.id = :lessonId AND pa.learner.id = :learnerId")
    Double findBestScoreByLessonAndLearner(@Param("lessonId") Long lessonId, @Param("learnerId") Long learnerId);

    org.springframework.data.domain.Page<PracticeAttempt> findByLearnerId(Long learnerId, org.springframework.data.domain.Pageable pageable);
    
    org.springframework.data.domain.Page<PracticeAttempt> findByLearnerIdAndExerciseId(Long learnerId, Long exerciseId, org.springframework.data.domain.Pageable pageable);
    
    org.springframework.data.domain.Page<PracticeAttempt> findByExerciseLessonIdAndLearnerId(Long lessonId, Long learnerId, org.springframework.data.domain.Pageable pageable);
    
    java.util.Optional<PracticeAttempt> findByClientUuid(String clientUuid);
}

