package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Exercises;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercises, Long> {
    List<Exercises> findByLessonId(Long lessonId);
}
