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
            System.out.println("Successfully dropped redundant 'id' column from 'users' table.");
        } catch (Exception e) {
            System.err.println("Error dropping 'id' column: " + e.getMessage());
        }
    }
}
