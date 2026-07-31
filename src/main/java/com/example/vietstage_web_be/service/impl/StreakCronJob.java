package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.entity.LearnerProfile;
import com.example.vietstage_web_be.repository.LearnerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StreakCronJob {

    private final LearnerProfileRepository learnerProfileRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void resetMissedStreaks() {
        log.info("Running daily streak reset job...");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        // Find all profiles where lastPracticeDate is before yesterday AND currentStreak > 0
        // For simplicity and efficiency without writing a custom query, we can query all or write a custom query.
        // It is better to write a custom query. I will add it to the repository in the next step.
        List<LearnerProfile> profilesToReset = learnerProfileRepository.findProfilesToResetStreak(yesterday);
        
        for (LearnerProfile profile : profilesToReset) {
            profile.setCurrentStreak(0);
        }
        
        learnerProfileRepository.saveAll(profilesToReset);
        log.info("Reset streaks for {} learners.", profilesToReset.size());
    }
}