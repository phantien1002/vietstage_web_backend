package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.PracticeSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {
    Page<PracticeSession> findByLearnerId(Long learnerId, Pageable pageable);
}