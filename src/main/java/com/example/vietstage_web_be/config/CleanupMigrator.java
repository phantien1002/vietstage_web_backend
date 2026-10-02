package com.example.vietstage_web_be.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

// @Component
@RequiredArgsConstructor
public class CleanupMigrator implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP COLUMN IF EXISTS id");
            
            try {
                jdbcTemplate.execute("ALTER TABLE quiz_attempts DROP CONSTRAINT IF EXISTS uk_quiz_client_attempt");
                jdbcTemplate.execute("ALTER TABLE quiz_attempts DROP CONSTRAINT IF EXISTS quiz_attempts_client_attempt_id_key");
                jdbcTemplate.execute("ALTER TABLE quiz_attempts ADD CONSTRAINT uk_quiz_client_attempt UNIQUE (learner_id, client_attempt_id)");
                System.out.println("Updated unique constraints for quiz_attempts.");
            } catch (Exception ex) {
                System.err.println("Notice: Could not update quiz_attempts constraint: " + ex.getMessage());
            }

            try {
                jdbcTemplate.execute("ALTER TABLE minigame_attempts DROP CONSTRAINT IF EXISTS uk_minigame_client_attempt");
                jdbcTemplate.execute("ALTER TABLE minigame_attempts DROP CONSTRAINT IF EXISTS minigame_attempts_client_attempt_id_key");
                jdbcTemplate.execute("ALTER TABLE minigame_attempts ADD CONSTRAINT uk_minigame_client_attempt UNIQUE (learner_id, client_attempt_id)");
                System.out.println("Updated unique constraints for minigame_attempts.");
            } catch (Exception ex) {
                System.err.println("Notice: Could not update minigame_attempts constraint: " + ex.getMessage());
            }

            try {
                jdbcTemplate.execute("ALTER TABLE quizzes ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'ACTIVE'");
                jdbcTemplate.execute("UPDATE quizzes SET status = 'ACTIVE' WHERE status IS NULL");
                jdbcTemplate.execute("ALTER TABLE minigame_challenges ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'ACTIVE'");
                jdbcTemplate.execute("UPDATE minigame_challenges SET status = 'ACTIVE' WHERE status IS NULL");
                System.out.println("Updated status columns for quizzes and minigames.");
            } catch (Exception ex) {
                System.err.println("Notice: Could not add status columns: " + ex.getMessage());
            }

            try {
                jdbcTemplate.execute("ALTER TABLE learner_cosmetics DROP CONSTRAINT IF EXISTS uk_learner_cosmetic_client_request");
                jdbcTemplate.execute("ALTER TABLE learner_cosmetics DROP CONSTRAINT IF EXISTS learner_cosmetics_client_request_id_key");
                jdbcTemplate.execute("ALTER TABLE learner_cosmetics ADD CONSTRAINT uk_learner_cosmetic_client_request UNIQUE (learner_id, client_request_id)");
                System.out.println("Updated unique constraints for learner_cosmetics.");
            } catch (Exception ex) {
                System.err.println("Notice: Could not update learner_cosmetics constraint: " + ex.getMessage());
            }

            try {
                jdbcTemplate.execute("INSERT INTO users (email, username, password_hash, role_id, created_at, status) " +
                    "SELECT 'phuclong2710@gmail.com', 'Phuc Long', 'hashed_pass', r.id, CURRENT_TIMESTAMP, 'ACTIVE' " +
                    "FROM roles r WHERE r.name = 'LEARNER' " +
                    "AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'phuclong2710@gmail.com')");
                
                jdbcTemplate.execute("INSERT INTO learner_profiles (user_id, has_full_access, created_at, streak_days, current_streak, longest_streak, total_points, total_stars, spendable_stars) " +
                    "SELECT id, TRUE, CURRENT_TIMESTAMP, 0, 0, 0, 0, 0, 0 FROM users WHERE email IN ('phuclong2710@gmail.com', 'thanhdattb19@gmail.com') " +
                    "AND NOT EXISTS (SELECT 1 FROM learner_profiles lp WHERE lp.user_id = users.id)");

                jdbcTemplate.execute("UPDATE learner_profiles SET has_full_access = TRUE " +
                    "WHERE user_id IN (SELECT id FROM users WHERE email IN ('phuclong2710@gmail.com', 'thanhdattb19@gmail.com'))");
                    
                System.out.println("Granted full access to test accounts.");
            } catch (Exception ex) {
                System.err.println("Notice: Could not update full access: " + ex.getMessage());
            }

            System.out.println("Successfully ran DB cleanups.");
        } catch (Exception e) {
            System.err.println("Error dropping 'id' column: " + e.getMessage());
        }
    }
}
