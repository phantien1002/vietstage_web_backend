package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.PracticeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

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

    @Query("SELECT pa FROM PracticeAttempt pa " +
            "JOIN FETCH pa.exercise e " +
            "LEFT JOIN FETCH e.lesson l " +
            "JOIN FETCH pa.learner u " +
            "WHERE l.createdBy.id = :instructorId " +
            "AND (:learnerId IS NULL OR u.id = :learnerId) " +
            "AND (:lessonId IS NULL OR l.id = :lessonId) " +
            "AND (pa.createdAt >= :fromDateTime) " +
            "AND (pa.createdAt <= :toDateTime) " +
            "ORDER BY pa.createdAt DESC")
    Page<PracticeAttempt> findFilteredAttempts(
            @Param("instructorId") Long instructorId,
            @Param("learnerId") Long learnerId,
            @Param("lessonId") Long lessonId,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            Pageable pageable
    );

    @Query(value = "SELECT " +
            "CASE " +
            "   WHEN CAST(:groupBy AS text) = 'week' THEN TO_CHAR(pa.created_at, 'YYYY-\"W\"IW') " +
            "   WHEN CAST(:groupBy AS text) = 'month' THEN TO_CHAR(pa.created_at, 'YYYY-MM') " +
            "   ELSE TO_CHAR(pa.created_at, 'YYYY-MM-DD') " +
            "END AS timeGroup, " +
            "COUNT(pa.id) AS totalAttempts, " +
            "AVG(pa.total_score) AS averageTotalScore, " +
            "SUM(pa.points_earned) AS totalPointsEarned, " +
            "SUM(pa.stars) AS totalStarsEarned " +
            "FROM practice_attempts pa " +
            "JOIN exercises e ON pa.exercise_id = e.id " +
            "JOIN lessons l ON e.lesson_id = l.id " +
            "WHERE l.created_by = :instructorId " +
            "AND (:learnerId IS NULL OR pa.learner_id = :learnerId) " +
            "AND (:lessonId IS NULL OR e.lesson_id = :lessonId) " +
            "AND (pa.created_at >= :fromDateTime) " +
            "AND (pa.created_at <= :toDateTime) " +
            "GROUP BY timeGroup " +
            "ORDER BY timeGroup ASC", nativeQuery = true)
    List<Object[]> findGroupedPracticeStats(
            @Param("instructorId") Long instructorId,
            @Param("learnerId") Long learnerId,
            @Param("lessonId") Long lessonId,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            @Param("groupBy") String groupBy
    );
}

