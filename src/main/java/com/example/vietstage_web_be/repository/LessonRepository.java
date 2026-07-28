package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long>, JpaSpecificationExecutor<Lesson> {
    boolean existsByTitleIgnoreCaseAndInstrumentId(String title, Long instrumentId);
    java.util.Optional<Lesson> findTopByOrderByIdDesc();
}
