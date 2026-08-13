package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long>, JpaSpecificationExecutor<Lesson> {
    boolean existsByTitleIgnoreCaseAndInstrumentId(String title, Long instrumentId);
    java.util.Optional<Lesson> findTopByOrderByIdDesc();
    java.util.List<Lesson> findByStatusIgnoreCase(String status);
    Optional<Lesson> findById(Long id);
    List<Lesson> findByInstrumentId(Long instrumentId);
}
