package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LearnerDailyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearnerDailyChallengeRepository extends JpaRepository<LearnerDailyChallenge, Long> {
    Optional<LearnerDailyChallenge> findByLearnerIdAndChallengeId(Long learnerId, Long challengeId);
    boolean existsByLearnerIdAndChallengeId(Long learnerId, Long challengeId);
}