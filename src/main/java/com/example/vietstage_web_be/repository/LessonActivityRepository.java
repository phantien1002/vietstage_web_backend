package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LessonActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonActivityRepository extends JpaRepository<LessonActivity, String> {
    List<LessonActivity> findByLessonCodeAndRevisionOrderByOrderIndexAsc(String lessonCode, Integer revision);
}
