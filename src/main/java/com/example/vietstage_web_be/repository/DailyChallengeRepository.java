package com.example.vietstage_web_be.repository;

import com.example.vietstage_web_be.entity.DailyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, Long> {
    List<DailyChallenge> findByChallengeDate(LocalDate date);
}