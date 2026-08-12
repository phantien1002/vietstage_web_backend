package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByLessonIdOrderByOrderIndexAsc(Long lessonId);

    @Query("""
        SELECT q
        FROM Quiz q
        JOIN FETCH q.lesson l
        JOIN FETCH l.skillLevel sl
        WHERE sl.orderIndex <= :maxLevel
        ORDER BY sl.orderIndex ASC,
                 l.orderIndex ASC,
                 q.orderIndex ASC
    """)
    List<Quiz> findByMaxLearnerLevel(@Param("maxLevel") Short maxLevel);

    @Query("""
            SELECT q
            FROM Quiz q
            JOIN FETCH q.lesson l
            JOIN FETCH l.skillLevel sl
            WHERE sl.id = :skillLevelId
            ORDER BY l.orderIndex ASC,
                     q.orderIndex ASC
            """)
    List<Quiz> findBySkillLevelId(@Param("skillLevelId") Long skillLevelId);
}