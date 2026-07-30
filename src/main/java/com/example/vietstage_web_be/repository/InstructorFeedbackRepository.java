package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.InstructorFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorFeedbackRepository extends JpaRepository<InstructorFeedback, Long> {
    List<InstructorFeedback> findByPracticeAttemptId(Long attemptId);
    Optional<InstructorFeedback> findByPracticeAttemptIdAndInstructorId(Long attemptId, Long instructorId);
}