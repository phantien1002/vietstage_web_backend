package com.example.vietstage_web_be.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting DemoDataSeeder...");

        String[] demoEmails = {"phuclong2710@gmail.com", "thanhdattb19@gmail.com"};

        for (String email : demoEmails) {
            // Find user id
            List<Long> userIds = jdbcTemplate.queryForList(
                    "SELECT user_id FROM users WHERE email = ?", Long.class, email);

            if (userIds.isEmpty()) {
                log.warn("Demo user not found: {}", email);
                continue;
            }

            Long uid = userIds.get(0);
            
            // Upsert learner_profile if missing
            jdbcTemplate.execute(
                "INSERT INTO learner_profiles (user_id, has_full_access, total_stars, spendable_stars) " +
                "VALUES (" + uid + ", true, 0, 0) " +
                "ON CONFLICT (user_id) DO UPDATE SET has_full_access = true"
            );

            // Fetch all visible approved lessons for Dan Tranh (1) and Sao Truc (2)
            List<Long> lessonIds = jdbcTemplate.queryForList(
                    "SELECT lesson_id FROM lessons WHERE instrument_id IN (1, 2) AND status = 'APPROVED' AND is_visible = true", Long.class);

            for (Long lid : lessonIds) {
                // Upsert lesson completion
                String attemptId = "auto_complete_" + uid + "_" + lid;
                jdbcTemplate.update(
                        "INSERT INTO learner_lesson_progress (" +
                        "learner_user_id, lesson_id, status, stars, best_score, " +
                        "unlocked_at, started_at, completed_at, updated_at, last_client_attempt_id" +
                        ") VALUES (?, ?, 'COMPLETED', 3, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?) " +
                        "ON CONFLICT DO NOTHING", // PostgreSQL doesn't have ON CONFLICT on surrogate id, we will do query first
                        uid, lid, attemptId
                );
                
                // fallback update for existing
                jdbcTemplate.update(
                    "UPDATE learner_lesson_progress SET status = 'COMPLETED', stars = 3, best_score = 100, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE learner_user_id = ? AND lesson_id = ?", uid, lid
                );
            }

            // Recalculate stars
            Integer totalStars = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(stars), 0) FROM learner_lesson_progress WHERE learner_user_id = ? AND status = 'COMPLETED'",
                    Integer.class, uid);

            jdbcTemplate.update(
                    "UPDATE learner_profiles SET total_stars = ?, spendable_stars = GREATEST(spendable_stars, ?), updated_at = CURRENT_TIMESTAMP WHERE user_id = ?",
                    totalStars, totalStars, uid);

            // Seed practice sessions and attempts for instrument 1 (Dan Tranh)
            seedAttemptForInstrument(uid, 1L);
            // Seed practice sessions and attempts for instrument 2 (Sao Truc)
            seedAttemptForInstrument(uid, 2L);
            
            seedUsageSessions(uid, email);
            
            log.info("DemoDataSeeder completed for {}", email);
        }
        
        log.info("DemoDataSeeder finished.");
    }
    
    private void seedAttemptForInstrument(Long uid, Long instrumentId) {
        List<Long> exercises = jdbcTemplate.queryForList(
            "SELECT e.id FROM exercises e JOIN lessons l ON e.lesson_id = l.lesson_id WHERE l.instrument_id = ? LIMIT 1",
            Long.class, instrumentId
        );
        
        Long exId = null;
        if (exercises.isEmpty()) {
            // Mock exercise if none exists
            List<Long> lessons = jdbcTemplate.queryForList(
                "SELECT lesson_id FROM lessons WHERE instrument_id = ? LIMIT 1", Long.class, instrumentId);
            if (!lessons.isEmpty()) {
                jdbcTemplate.update(
                    "INSERT INTO exercises (lesson_id, title, description, order_index, created_at, updated_at) " +
                    "VALUES (?, 'Mock Exercise', 'Demo', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", lessons.get(0));
                
                exercises = jdbcTemplate.queryForList(
                    "SELECT e.id FROM exercises e JOIN lessons l ON e.lesson_id = l.lesson_id WHERE l.instrument_id = ? LIMIT 1",
                    Long.class, instrumentId
                );
                if (!exercises.isEmpty()) {
                    exId = exercises.get(0);
                }
            }
        } else {
            exId = exercises.get(0);
        }
        
        if (exId != null) {
            // check if attempt exists
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM practice_attempts WHERE learner_id = ? AND exercise_id = ?",
                Integer.class, uid, exId);
            
            if (count != null && count == 0) {
                int[] daysAgo = {0, 2, 5, 10, 15, 20}; // multiple days in past
                for (int days : daysAgo) {
                    // Insert session
                    jdbcTemplate.update(
                        "INSERT INTO practice_sessions (learner_id, started_at, ended_at, duration_minutes) " +
                        "VALUES (?, CURRENT_TIMESTAMP - (INTERVAL '1 day' * ?), CURRENT_TIMESTAMP - (INTERVAL '1 day' * ?) + INTERVAL '30 minutes', 30)",
                        uid, days, days
                    );
                    
                    Long sessionId = jdbcTemplate.queryForObject(
                        "SELECT id FROM practice_sessions WHERE learner_id = ? ORDER BY id DESC LIMIT 1",
                        Long.class, uid
                    );
                    
                    if (sessionId != null) {
                        jdbcTemplate.update(
                            "INSERT INTO practice_attempts (" +
                            "client_uuid, learner_id, exercise_id, session_id, started_at, completed_at, created_at, " +
                            "pitch_score, rhythm_score, total_score, stars, points_earned, sync_status" +
                            ") VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP - (INTERVAL '1 day' * ?), CURRENT_TIMESTAMP - (INTERVAL '1 day' * ?), CURRENT_TIMESTAMP - (INTERVAL '1 day' * ?), 100, 100, 100, 3, 50, 'SYNCED')",
                            UUID.randomUUID().toString(), uid, exId, sessionId, days, days, days
                        );
                    }
                }
            }
        }
    }
    
    private void seedUsageSessions(Long uid, String email) {
        Integer sessionCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM usage_sessions WHERE user_id = ?", Integer.class, uid
        );
        
        if (sessionCount != null && sessionCount == 0) {
            // Seed a few past usage sessions for retention and dashboard graphs
            int[] daysAgo = {0, 1, 2, 5, 7, 10, 15, 20, 30};
            for (int days : daysAgo) {
                jdbcTemplate.update(
                    "INSERT INTO usage_sessions (usage_session_id, user_id, platform, started_at, ended_at) " +
                    "VALUES (?, ?, 'WEB', CURRENT_TIMESTAMP - (INTERVAL '1 day' * ?), CURRENT_TIMESTAMP - (INTERVAL '1 day' * ?) + INTERVAL '30 minutes')",
                    UUID.randomUUID(), uid, days, days
                );
            }
            log.info("Seeded usage_sessions for {}", email);
        }
    }
}
