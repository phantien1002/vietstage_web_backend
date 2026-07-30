package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.MinigameAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MinigameAttemptRepository extends JpaRepository<MinigameAttempt, Long> {
    Page<MinigameAttempt> findByChallengeIdAndLearnerIdOrderByCompletedAtDesc(Long challengeId, Long learnerId, Pageable pageable);
}