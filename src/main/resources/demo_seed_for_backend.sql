-- Seed script for Full Access users and Demo Learners
BEGIN;

-- 1. CLEAN UP MOCK LEARNERS
-- Remove usage_sessions, practice_attempts, lesson_completions, learner_profiles, and users
DELETE FROM usage_sessions WHERE user_id IN (SELECT user_id FROM users WHERE user_code LIKE 'LNR_MOCK_%');
DELETE FROM practice_attempts WHERE learner_id IN (SELECT user_id FROM users WHERE user_code LIKE 'LNR_MOCK_%');
DELETE FROM learner_lesson_progress WHERE learner_user_id IN (SELECT user_id FROM users WHERE user_code LIKE 'LNR_MOCK_%');
DELETE FROM learner_profiles WHERE user_id IN (SELECT user_id FROM users WHERE user_code LIKE 'LNR_MOCK_%');
DELETE FROM users WHERE user_code LIKE 'LNR_MOCK_%';

-- 2. GRANT FULL ACCESS TO SPECIFIED ACCOUNTS (idempotent, supports new lessons)
DO $$
DECLARE
    u_id BIGINT;
BEGIN
    FOR u_id IN SELECT user_id FROM users WHERE email IN ('phuclong2710@gmail.com', 'thanhdattb19@gmail.com') LOOP
        -- Insert missing completions
        INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score, unlocked_at, started_at, completed_at, updated_at)
        SELECT u_id, lesson_id, 'COMPLETED', 3, 100, NOW(), NOW(), NOW(), NOW()
        FROM lessons l
        WHERE NOT EXISTS (SELECT 1 FROM learner_lesson_progress lc WHERE lc.learner_user_id = u_id AND lc.lesson_id = l.lesson_id);

        -- Update all completions to 3 stars, completed status
        UPDATE learner_lesson_progress 
        SET status = 'COMPLETED', stars = 3, best_score = 100, completed_at = COALESCE(completed_at, NOW()), updated_at = NOW()
        WHERE learner_user_id = u_id;

        -- Update total stars in learner_profile
        UPDATE learner_profiles
        SET total_stars = (SELECT COALESCE(SUM(stars),0) FROM learner_lesson_progress WHERE learner_user_id = u_id),
            spendable_stars = (SELECT COALESCE(SUM(stars),0) FROM learner_lesson_progress WHERE learner_user_id = u_id)
        WHERE user_id = u_id;
    END LOOP;
END $$;

-- 3. SEED FIXED DEMO LEARNERS
DO $$
DECLARE
    demo_users JSONB := '[
        {"email":"minhanh.demo@vietstage.com","name":"Nguyễn Minh Anh","code":"LNR_DEMO_001"},
        {"email":"giahuy.demo@vietstage.com","name":"Trần Gia Huy","code":"LNR_DEMO_002"},
        {"email":"khanhlinh.demo@vietstage.com","name":"Lê Khánh Linh","code":"LNR_DEMO_003"},
        {"email":"quocbao.demo@vietstage.com","name":"Phạm Quốc Bảo","code":"LNR_DEMO_004"},
        {"email":"ngocmai.demo@vietstage.com","name":"Vũ Ngọc Mai","code":"LNR_DEMO_005"}
    ]';
    u JSONB;
    r_id BIGINT;
    new_u_id BIGINT;
    les_id BIGINT;
BEGIN
    SELECT role_id INTO r_id FROM roles WHERE role_name = 'LEARNER';
    FOR u IN SELECT * FROM jsonb_array_elements(demo_users) LOOP
        -- Insert user if not exists
        IF NOT EXISTS (SELECT 1 FROM users WHERE email = u->>'email') THEN
            INSERT INTO users (email, full_name, user_code, role_id, password_hash, is_active)
            VALUES (u->>'email', u->>'name', u->>'code', r_id, '$2a$10$xyz', true)
            RETURNING user_id INTO new_u_id;
        ELSE
            UPDATE users SET full_name = u->>'name', user_code = u->>'code' WHERE email = u->>'email' RETURNING user_id INTO new_u_id;
        END IF;

        -- Profile
        IF NOT EXISTS (SELECT 1 FROM learner_profiles WHERE user_id = new_u_id) THEN
            INSERT INTO learner_profiles (user_id, current_streak, longest_streak, total_points, total_stars, spendable_stars, updated_at)
            VALUES (new_u_id, 2, 5, 1500, 15, 15, NOW());
        END IF;

        -- Seed random progress for 5 lessons
        FOR les_id IN SELECT lesson_id FROM lessons ORDER BY random() LIMIT 5 LOOP
            IF NOT EXISTS (SELECT 1 FROM learner_lesson_progress WHERE learner_user_id = new_u_id AND lesson_id = les_id) THEN
                INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score, unlocked_at, started_at, completed_at, updated_at)
                VALUES (new_u_id, les_id, 'COMPLETED', floor(random()*3 + 1), floor(random()*40 + 60), NOW() - interval '2 days', NOW() - interval '1 day', NOW(), NOW());
            END IF;
            
            IF NOT EXISTS (SELECT 1 FROM practice_attempts WHERE learner_id = new_u_id AND exercise_id IN (SELECT id FROM exercises WHERE lesson_id = les_id)) THEN
                INSERT INTO practice_attempts (learner_id, exercise_id, pitch_score, rhythm_score, dynamics_score, total_score, stars, points_earned, created_at)
                SELECT new_u_id, e.id, 90, 85, 88, 88, 2, 50, NOW() FROM exercises e WHERE e.lesson_id = les_id LIMIT 1;
            END IF;
        END LOOP;
        
        -- Sync stars based on completions
        UPDATE learner_profiles
        SET total_stars = (SELECT COALESCE(SUM(stars),0) FROM learner_lesson_progress WHERE learner_user_id = new_u_id),
            spendable_stars = (SELECT COALESCE(SUM(stars),0) FROM learner_lesson_progress WHERE learner_user_id = new_u_id)
        WHERE user_id = new_u_id;
    END LOOP;
END $$;

COMMIT;
