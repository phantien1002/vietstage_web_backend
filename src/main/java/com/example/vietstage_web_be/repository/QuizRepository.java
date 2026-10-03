package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @EntityGraph(attributePaths = {"lesson", "lesson.createdBy"})
    Optional<Quiz> findById(Long id);

    List<Quiz> findByLessonIdOrderByOrderIndexAsc(Long lessonId);

    List<Quiz> findByInstrumentIdOrderByOrderIndexAsc(Long instrumentId);
}