package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.MinigameAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MinigameAttemptRepository extends JpaRepository<MinigameAttempt, Long> {
    Page<MinigameAttempt> findByChallengeIdAndLearnerIdOrderByCompletedAtDesc(Long challengeId, Long learnerId, Pageable pageable);
    
    java.util.Optional<MinigameAttempt> findByClientAttemptId(String clientAttemptId);

    java.util.Optional<MinigameAttempt> findByClientAttemptIdAndLearnerId(String clientAttemptId, Long learnerId);

    Page<MinigameAttempt> findByLearnerId(Long learnerId, Pageable pageable);

    java.util.Optional<MinigameAttempt> findByIdAndLearnerId(Long id, Long learnerId);

    @Query("SELECT COUNT(DISTINCT ma.learner.id) as learnersCount, COUNT(ma) as totalAttempts, " +
           "AVG(ma.score) as averageScore, AVG(ma.starsEarned) as averageStars " +
           "FROM MinigameAttempt ma WHERE ma.challenge.id = :minigameId")
    java.util.Map<String, Object> getMinigameStatistics(@Param("minigameId") Long minigameId);
    
    @Query("SELECT COUNT(DISTINCT ma.learner.id) as learnersCount, COUNT(ma) as totalAttempts, " +
           "AVG(ma.score) as averageScore, AVG(ma.starsEarned) as averageStars " +
           "FROM MinigameAttempt ma WHERE ma.challenge.lesson.id = :lessonId")
    java.util.Map<String, Object> getMinigameStatisticsByLesson(@Param("lessonId") Long lessonId);
}
