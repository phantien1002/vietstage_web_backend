package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LessonContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonContentRepository extends JpaRepository<LessonContent, Long> {
    List<LessonContent> findByLessonIdOrderByOrderIndexAsc(Long lessonId);
}