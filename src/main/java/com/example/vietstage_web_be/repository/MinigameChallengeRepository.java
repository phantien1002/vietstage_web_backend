package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.MinigameChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MinigameChallengeRepository extends JpaRepository<MinigameChallenge, Long> {
    List<MinigameChallenge> findByLessonIdOrderByOrderIndexAsc(Long lessonId);
}