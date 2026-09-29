package com.example.vietstage_web_be.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
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

            System.out.println("Successfully ran DB cleanups.");
        } catch (Exception e) {
            System.err.println("Error dropping 'id' column: " + e.getMessage());
        }
    }
}
