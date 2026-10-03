package com.example.vietstage_web_be.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurriculumSeeder {

    private final JdbcTemplate jdbcTemplate;

    // DISABLED: @EventListener(ApplicationReadyEvent.class)
    public void seedCurriculum() {
        try {
            log.info("Starting Curriculum Seed (Đàn Tranh & Sáo Trúc)...");
            
            // Add unique constraint to lesson_code safely if it does not exist
            try {
                jdbcTemplate.execute("""
                    DO $$ 
                    BEGIN 
                        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'unique_lesson_code') THEN 
                            ALTER TABLE lessons ADD CONSTRAINT unique_lesson_code UNIQUE (lesson_code); 
                        END IF; 
                        
                        -- Drop conflicting order_index constraint to allow curriculum seeding
                        ALTER TABLE lessons DROP CONSTRAINT IF EXISTS lessons_instrument_id_skill_level_id_order_index_key;
                    END $$;
                """);
            } catch (Exception e) {
                log.warn("Could not enforce unique constraint on lesson_code: {}", e.getMessage());
            }

            ClassPathResource resource = new ClassPathResource("curriculum_seed_for_backend.sql");
            if (!resource.exists()) {
                log.warn("Seed file curriculum_seed_for_backend.sql not found in resources!");
                return;
            }

            String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            jdbcTemplate.execute(sql);
            log.info("Curriculum Seed completed successfully.");
            
            // Execute Demo & Full Access Seeder
            log.info("Starting Demo Learners & Full Access Seed...");
            ClassPathResource demoResource = new ClassPathResource("demo_seed_for_backend.sql");
            if (demoResource.exists()) {
                String demoSql = StreamUtils.copyToString(demoResource.getInputStream(), StandardCharsets.UTF_8);
                jdbcTemplate.execute(demoSql);
                log.info("Demo Seed completed successfully.");
            }
        } catch (Exception e) {
            log.error("Failed to execute curriculum seed script", e);
        }
    }
}
