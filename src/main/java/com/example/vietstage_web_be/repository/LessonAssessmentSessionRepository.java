package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LessonAssessmentSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LessonAssessmentSessionRepository extends JpaRepository<LessonAssessmentSession, Long> {
    Optional<LessonAssessmentSession> findByLearnerIdAndClientSessionId(Long learnerId, String clientSessionId);
    Page<LessonAssessmentSession> findByLearnerId(Long learnerId, Pageable pageable);
    Optional<LessonAssessmentSession> findByIdAndLearnerId(Long id, Long learnerId);
}
