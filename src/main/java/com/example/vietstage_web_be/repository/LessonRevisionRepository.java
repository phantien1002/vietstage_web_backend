package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LessonRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRevisionRepository extends JpaRepository<LessonRevision, LessonRevision.LessonRevisionId> {
}
