package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LearnerAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearnerAchievementRepository extends JpaRepository<LearnerAchievement, Long> {
    List<LearnerAchievement> findByLearnerId(Long learnerId);
    Optional<LearnerAchievement> findByLearnerIdAndAchievementId(Long learnerId, Long achievementId);
}