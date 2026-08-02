package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByLessonIdOrderByOrderIndexAsc(Long lessonId);
}