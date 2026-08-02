package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.LearnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LearnerProfileRepository extends JpaRepository<LearnerProfile, Long> {
    java.util.Optional<LearnerProfile> findByUserId(Long userId);
    
    @Query("SELECT lp FROM LearnerProfile lp WHERE (lp.lastPracticeDate IS NULL OR lp.lastPracticeDate < :yesterday) AND lp.currentStreak > 0")
    List<LearnerProfile> findProfilesToResetStreak(@Param("yesterday") LocalDate yesterday);
}