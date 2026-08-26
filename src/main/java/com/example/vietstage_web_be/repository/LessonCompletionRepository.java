package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LessonCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion,Long> {
    @Query("SELECT l.id AS lessonId, l.title AS title, COALESCE(lc.stars, 0) AS stars, CASE WHEN lc.status = 'COMPLETED' THEN true ELSE false END AS completed, " +
           "l.lessonCode AS lessonCode, l.instrument.instrumentCode AS instrumentCode, l.skillLevel.levelCode AS levelCode, l.orderIndex AS orderIndex, " +
           "lc.bestScore AS highestScore, lc.completedAt AS completedAt " +
            "FROM Lesson l " +
            "LEFT JOIN LessonCompletion lc ON l.id = lc.lesson.id AND lc.learner.id = :learnerId " +
            "WHERE (:instrumentId IS NULL OR l.instrument.id = :instrumentId) " +
            "  AND (:skillLevelId IS NULL OR l.skillLevel.id = :skillLevelId) " +
            "ORDER BY l.orderIndex ASC")
    List<Object[]> findLearnerProgressList(
            @Param("learnerId") Long learnerId,
            @Param("instrumentId") Long instrumentId,
            @Param("skillLevelId") Long skillLevelId
    );

    // Tính tổng số sao đạt được của Learner
    @Query("SELECT COALESCE(SUM(lc.stars), 0) FROM LessonCompletion lc WHERE lc.learner.id = :learnerId")
    Integer sumTotalStarsByLearnerId(@Param("learnerId") Long learnerId);

    // Đếm số bài học đã hoàn thành
    @Query("SELECT COUNT(lc) FROM LessonCompletion lc WHERE lc.learner.id = :learnerId AND lc.status = 'COMPLETED'")
    Long countCompletedLessonsByLearnerId(@Param("learnerId") Long learnerId);

    // Lấy tiến độ của 1 Learner trên 1 Lesson cụ thể
    @Query("SELECT lc FROM LessonCompletion lc WHERE lc.lesson.id = :lessonId AND lc.learner.id = :learnerId")
    Optional<LessonCompletion> findByLessonIdAndLearnerId(@Param("lessonId") Long lessonId, @Param("learnerId") Long learnerId);

}

