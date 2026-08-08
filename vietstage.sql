-- =========================================================
-- VIETSTAGE POSTGRESQL SCRIPT v5.1 (CORRECTED)
-- Scope source: VietStage Capstone Project Register
-- Principles:
--   1) PostgreSQL stores user data, progress and lesson metadata.
--   2) Redis stores auth refresh sessions and leaderboard cache.
--   3) No PostgreSQL tables for auth sessions or leaderboards.
--   4) Offline sync uses client_uuid for idempotency.
--   5) Only five practice metrics are stored:
--      pitch, rhythm, tonal quality, breath and dynamics.
--   6) learner_profiles and instructor_profiles are separate.
--   7) Skill levels are normalized through skill_levels.
-- =========================================================

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- DROP TABLES (dependency order)
-- =========================================================
DROP TABLE IF EXISTS app_configs CASCADE;
DROP TABLE IF EXISTS content_reviews CASCADE;
DROP TABLE IF EXISTS usage_sessions CASCADE;
DROP TABLE IF EXISTS learner_daily_challenges CASCADE;
DROP TABLE IF EXISTS daily_challenges CASCADE;
DROP TABLE IF EXISTS learner_cosmetics CASCADE;
DROP TABLE IF EXISTS cosmetic_items CASCADE;
DROP TABLE IF EXISTS learner_achievements CASCADE;
DROP TABLE IF EXISTS achievements CASCADE;
DROP TABLE IF EXISTS learner_lesson_progress CASCADE;
DROP TABLE IF EXISTS minigame_attempts CASCADE;
DROP TABLE IF EXISTS minigame_challenges CASCADE;
DROP TABLE IF EXISTS quiz_answers CASCADE;
DROP TABLE IF EXISTS quiz_attempts CASCADE;
DROP TABLE IF EXISTS quiz_options CASCADE;
DROP TABLE IF EXISTS quiz_questions CASCADE;
DROP TABLE IF EXISTS quizzes CASCADE;
DROP TABLE IF EXISTS instructor_feedback CASCADE;
DROP TABLE IF EXISTS practice_attempts CASCADE;
DROP TABLE IF EXISTS exercises CASCADE;
DROP TABLE IF EXISTS media_assets CASCADE;
DROP TABLE IF EXISTS lesson_techniques CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS techniques CASCADE;
DROP TABLE IF EXISTS instructor_instruments CASCADE;
DROP TABLE IF EXISTS learner_instruments CASCADE;
DROP TABLE IF EXISTS instruments CASCADE;
DROP TABLE IF EXISTS instructor_profiles CASCADE;
DROP TABLE IF EXISTS learner_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS skill_levels CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- Remove deprecated v3 tables if this script upgrades an older database.
DROP TABLE IF EXISTS leaderboards CASCADE;
DROP TABLE IF EXISTS point_transactions CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS practice_sessions CASCADE;
DROP TABLE IF EXISTS lesson_completions CASCADE;
DROP TABLE IF EXISTS lesson_assets CASCADE;
DROP TABLE IF EXISTS lesson_contents CASCADE;
DROP TABLE IF EXISTS learner_progress CASCADE;
DROP TABLE IF EXISTS mini_games CASCADE;
DROP TABLE IF EXISTS lesson_mini_games CASCADE;
DROP TABLE IF EXISTS mini_game_results CASCADE;

-- =========================================================
-- 1. REFERENCE DATA
-- =========================================================
CREATE TABLE roles (
    role_id      BIGSERIAL PRIMARY KEY,
    role_name    VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT ck_roles_name
        CHECK (role_name IN ('ADMIN', 'INSTRUCTOR', 'LEARNER'))
);

CREATE TABLE skill_levels (
    skill_level_id BIGSERIAL PRIMARY KEY,
    level_code     VARCHAR(20) NOT NULL UNIQUE,
    level_name     VARCHAR(50) NOT NULL,
    order_index    SMALLINT NOT NULL UNIQUE,
    CONSTRAINT ck_skill_levels_code
        CHECK (level_code IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT ck_skill_levels_order
        CHECK (order_index > 0)
);

-- =========================================================
-- 2. USERS AND ROLE-SPECIFIC PROFILES
-- =========================================================
CREATE TABLE users (
    user_id        BIGSERIAL PRIMARY KEY,
      user_code      VARCHAR(50) UNIQUE,
      role_id        BIGINT NOT NULL REFERENCES roles(role_id) ON DELETE RESTRICT,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(150) NOT NULL,
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE learner_profiles (
    user_id                 BIGINT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    avatar_url              TEXT,
    total_practice_seconds  BIGINT NOT NULL DEFAULT 0,
    current_streak          INTEGER NOT NULL DEFAULT 0,
    longest_streak          INTEGER NOT NULL DEFAULT 0,
    last_practice_date      DATE,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_learner_profile_time CHECK (total_practice_seconds >= 0),
    CONSTRAINT ck_learner_profile_streaks
        CHECK (current_streak >= 0 AND longest_streak >= 0)
);

CREATE TABLE instructor_profiles (
    user_id           BIGINT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    biography         TEXT,
    years_experience  INTEGER NOT NULL DEFAULT 0,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_instructor_experience CHECK (years_experience >= 0)
);

-- =========================================================
-- 3. INSTRUMENTS, LEARNER SELECTION AND INSTRUCTOR EXPERTISE
-- =========================================================
CREATE TABLE instruments (
    id BIGSERIAL PRIMARY KEY,
    instrument_code VARCHAR(50) UNIQUE,
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   TEXT,
    icon_url      TEXT,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE learner_instruments (
    learner_user_id          BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    instrument_id            BIGINT NOT NULL REFERENCES instruments(id) ON DELETE RESTRICT,
    current_skill_level_id   BIGINT NOT NULL REFERENCES skill_levels(skill_level_id) ON DELETE RESTRICT,
    adaptive_skill_level_id  BIGINT NOT NULL REFERENCES skill_levels(skill_level_id) ON DELETE RESTRICT,
    selected_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (learner_user_id, instrument_id)
);

CREATE TABLE instructor_instruments (
    instructor_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    instrument_id      BIGINT NOT NULL REFERENCES instruments(id) ON DELETE RESTRICT,
    PRIMARY KEY (instructor_user_id, instrument_id)
);

CREATE TABLE techniques (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES instruments(id) ON DELETE RESTRICT,
    name          VARCHAR(120) NOT NULL,
    description   TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (instrument_id, name)
);

-- =========================================================
-- 4. LESSONS AND CONTENT
-- =========================================================
CREATE TABLE lessons (
    lesson_id            BIGSERIAL PRIMARY KEY,
    instrument_id        BIGINT NOT NULL REFERENCES instruments(id) ON DELETE RESTRICT,
    skill_level_id       BIGINT NOT NULL REFERENCES skill_levels(skill_level_id) ON DELETE RESTRICT,
    created_by_user_id   BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    title                VARCHAR(200) NOT NULL,
    description          TEXT,
    status               VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    order_index          INTEGER NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_lessons_status
        CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'ARCHIVED')),
    CONSTRAINT ck_lessons_order CHECK (order_index >= 0),
    UNIQUE (instrument_id, skill_level_id, order_index)
);

CREATE TABLE lesson_techniques (
    lesson_id     BIGINT NOT NULL REFERENCES lessons(lesson_id) ON DELETE CASCADE,
    technique_id  BIGINT NOT NULL REFERENCES techniques(technique_id) ON DELETE RESTRICT,
    PRIMARY KEY (lesson_id, technique_id)
);

CREATE TABLE media_assets (
    asset_id      BIGSERIAL PRIMARY KEY,
    lesson_id     BIGINT NOT NULL REFERENCES lessons(lesson_id) ON DELETE RESTRICT,
    asset_type    VARCHAR(30) NOT NULL,
    title         VARCHAR(200),
    asset_url     TEXT NOT NULL,
    tempo_bpm     INTEGER,
    duration_sec  NUMERIC(9,2),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_media_asset_type
        CHECK (asset_type IN ('REFERENCE_AUDIO', 'SHEET_IMAGE', 'TECHNIQUE_VIDEO', 'BEAT_MAP')),
    CONSTRAINT ck_media_tempo CHECK (tempo_bpm IS NULL OR tempo_bpm > 0),
    CONSTRAINT ck_media_duration CHECK (duration_sec IS NULL OR duration_sec >= 0)
);

-- =========================================================
-- 5. EXERCISES AND PRACTICE ATTEMPTS
-- =========================================================
CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    lesson_id           BIGINT NOT NULL REFERENCES lessons(lesson_id) ON DELETE RESTRICT,
    reference_asset_id  BIGINT REFERENCES media_assets(asset_id) ON DELETE SET NULL,
    beat_map_asset_id   BIGINT REFERENCES media_assets(asset_id) ON DELETE SET NULL,
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    pass_threshold      NUMERIC(5,2) NOT NULL DEFAULT 60.00,
    order_index         INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_exercise_threshold CHECK (pass_threshold BETWEEN 0 AND 100),
    CONSTRAINT ck_exercise_order CHECK (order_index >= 0),
    UNIQUE (lesson_id, order_index)
);

CREATE TABLE practice_attempts (
    id BIGSERIAL PRIMARY KEY,
    client_uuid         UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    learner_user_id     BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    exercise_id         BIGINT NOT NULL REFERENCES exercises(exercise_id) ON DELETE RESTRICT,
    started_at          TIMESTAMPTZ NOT NULL,
    completed_at        TIMESTAMPTZ NOT NULL,
    pitch_score         NUMERIC(5,2),
    rhythm_score        NUMERIC(5,2),
    tonal_quality_score NUMERIC(5,2),
    breath_score        NUMERIC(5,2),
    dynamics_score      NUMERIC(5,2),
    composite_score     NUMERIC(5,2),
    received_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_practice_time CHECK (completed_at >= started_at),
    CONSTRAINT ck_practice_pitch CHECK (pitch_score IS NULL OR pitch_score BETWEEN 0 AND 100),
    CONSTRAINT ck_practice_rhythm CHECK (rhythm_score IS NULL OR rhythm_score BETWEEN 0 AND 100),
    CONSTRAINT ck_practice_tonal CHECK (tonal_quality_score IS NULL OR tonal_quality_score BETWEEN 0 AND 100),
    CONSTRAINT ck_practice_breath CHECK (breath_score IS NULL OR breath_score BETWEEN 0 AND 100),
    CONSTRAINT ck_practice_dynamics CHECK (dynamics_score IS NULL OR dynamics_score BETWEEN 0 AND 100),
    CONSTRAINT ck_practice_composite CHECK (composite_score IS NULL OR composite_score BETWEEN 0 AND 100)
);

CREATE TABLE instructor_feedback (
    id BIGSERIAL PRIMARY KEY,
    attempt_id          BIGINT NOT NULL REFERENCES practice_attempts(attempt_id) ON DELETE RESTRICT,
    instructor_user_id  BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    comment             TEXT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- 6. QUIZZES
-- =========================================================
CREATE TABLE quizzes (
    id BIGSERIAL PRIMARY KEY,
    lesson_id     BIGINT NOT NULL REFERENCES lessons(lesson_id) ON DELETE RESTRICT,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    order_index   INTEGER NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_quiz_order CHECK (order_index >= 0),
    UNIQUE (lesson_id, order_index)
);

CREATE TABLE quiz_questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_id       BIGINT NOT NULL REFERENCES quizzes(quiz_id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    order_index   INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT ck_quiz_question_order CHECK (order_index >= 0),
    UNIQUE (quiz_id, order_index)
);

CREATE TABLE quiz_options (
    id BIGSERIAL PRIMARY KEY,
    question_id   BIGINT NOT NULL REFERENCES quiz_questions(question_id) ON DELETE CASCADE,
    option_text   TEXT NOT NULL,
    is_correct    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    client_uuid       UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    learner_user_id   BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    quiz_id           BIGINT NOT NULL REFERENCES quizzes(quiz_id) ON DELETE RESTRICT,
    score             NUMERIC(5,2) NOT NULL,
    started_at        TIMESTAMPTZ NOT NULL,
    completed_at      TIMESTAMPTZ NOT NULL,
    received_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_quiz_attempt_score CHECK (score BETWEEN 0 AND 100),
    CONSTRAINT ck_quiz_attempt_time CHECK (completed_at >= started_at)
);

CREATE TABLE quiz_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id          BIGINT NOT NULL REFERENCES quiz_attempts(attempt_id) ON DELETE CASCADE,
    question_id         BIGINT NOT NULL REFERENCES quiz_questions(question_id) ON DELETE RESTRICT,
    selected_option_id  BIGINT NOT NULL REFERENCES quiz_options(option_id) ON DELETE RESTRICT,
    UNIQUE (attempt_id, question_id)
);

-- =========================================================
-- 7. MINI-GAME CHALLENGES
-- =========================================================
CREATE TABLE minigame_challenges (
    id BIGSERIAL PRIMARY KEY,
    lesson_id          BIGINT NOT NULL REFERENCES lessons(lesson_id) ON DELETE RESTRICT,
    reference_asset_id BIGINT REFERENCES media_assets(asset_id) ON DELETE SET NULL,
    skill_level_id     BIGINT NOT NULL REFERENCES skill_levels(skill_level_id) ON DELETE RESTRICT,
    title              VARCHAR(200) NOT NULL,
    description        TEXT,
    challenge_type     VARCHAR(30) NOT NULL,
    content_json       JSONB,
    max_score          INTEGER NOT NULL DEFAULT 100,
    order_index        INTEGER NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_minigame_type
        CHECK (challenge_type IN ('RHYTHM_MATCH', 'MELODY_COMPLETE')),
    CONSTRAINT ck_minigame_max_score CHECK (max_score > 0),
    CONSTRAINT ck_minigame_order CHECK (order_index >= 0),
    UNIQUE (lesson_id, order_index)
);

CREATE TABLE minigame_attempts (
    id BIGSERIAL PRIMARY KEY,
    client_uuid       UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    learner_user_id   BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    challenge_id      BIGINT NOT NULL REFERENCES minigame_challenges(challenge_id) ON DELETE RESTRICT,
    score             INTEGER NOT NULL DEFAULT 0,
    started_at        TIMESTAMPTZ NOT NULL,
    completed_at      TIMESTAMPTZ NOT NULL,
    received_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_minigame_score CHECK (score >= 0),
    CONSTRAINT ck_minigame_time CHECK (completed_at >= started_at)
);

-- =========================================================
-- 8. LESSON PROGRESS, STARS AND UNLOCKING
-- =========================================================
CREATE TABLE learner_lesson_progress (
    learner_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    lesson_id       BIGINT NOT NULL REFERENCES lessons(lesson_id) ON DELETE RESTRICT,
    status          VARCHAR(20) NOT NULL DEFAULT 'LOCKED',
    stars           SMALLINT NOT NULL DEFAULT 0,
    best_score      NUMERIC(5,2),
    unlocked_at     TIMESTAMPTZ,
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (learner_user_id, lesson_id),
    CONSTRAINT ck_progress_status
        CHECK (status IN ('LOCKED', 'UNLOCKED', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT ck_progress_stars CHECK (stars BETWEEN 0 AND 3),
    CONSTRAINT ck_progress_score CHECK (best_score IS NULL OR best_score BETWEEN 0 AND 100)
);

-- =========================================================
-- 9. ACHIEVEMENTS AND COSMETIC REWARDS
-- =========================================================
CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    name           VARCHAR(120) NOT NULL UNIQUE,
    description    TEXT,
    icon_url       TEXT,
    condition_json JSONB NOT NULL
);

CREATE TABLE learner_achievements (
    learner_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    achievement_id  BIGINT NOT NULL REFERENCES achievements(achievement_id) ON DELETE RESTRICT,
    earned_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (learner_user_id, achievement_id)
);

CREATE TABLE cosmetic_items (
    id BIGSERIAL PRIMARY KEY,
    name             VARCHAR(120) NOT NULL UNIQUE,
    item_type        VARCHAR(30) NOT NULL,
    asset_url        TEXT,
    unlock_condition JSONB,
    CONSTRAINT ck_cosmetic_type
        CHECK (item_type IN ('ROOM_DECOR', 'AVATAR_SKIN', 'INSTRUMENT_SKIN'))
);

CREATE TABLE learner_cosmetics (
    learner_user_id  BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    cosmetic_item_id BIGINT NOT NULL REFERENCES cosmetic_items(cosmetic_item_id) ON DELETE RESTRICT,
    unlocked_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_equipped      BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (learner_user_id, cosmetic_item_id)
);

-- =========================================================
-- 10. DAILY CHALLENGES AND STREAK-RELATED PROGRESS
-- =========================================================
CREATE TABLE daily_challenges (
    id BIGSERIAL PRIMARY KEY,
    instrument_id  BIGINT REFERENCES instruments(id) ON DELETE RESTRICT,
    title          VARCHAR(200) NOT NULL,
    description    TEXT,
    challenge_date DATE NOT NULL,
    condition_json JSONB NOT NULL,
    UNIQUE (challenge_date, title)
);

CREATE TABLE learner_daily_challenges (
    learner_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    challenge_id    BIGINT NOT NULL REFERENCES daily_challenges(challenge_id) ON DELETE RESTRICT,
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    progress_value  NUMERIC(12,2) NOT NULL DEFAULT 0,
    completed_at    TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (learner_user_id, challenge_id),
    CONSTRAINT ck_daily_progress_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT ck_daily_progress_value CHECK (progress_value >= 0)
);

-- =========================================================
-- 11. CONTENT MODERATION
-- Each review targets exactly one lesson OR one media asset.
-- =========================================================
CREATE TABLE content_reviews (
    id BIGSERIAL PRIMARY KEY,
    reviewer_user_id  BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    lesson_id         BIGINT REFERENCES lessons(lesson_id) ON DELETE RESTRICT,
    media_asset_id    BIGINT REFERENCES media_assets(asset_id) ON DELETE RESTRICT,
    status            VARCHAR(30) NOT NULL,
    comment           TEXT,
    reviewed_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_content_review_target
        CHECK (
            (lesson_id IS NOT NULL AND media_asset_id IS NULL)
            OR
            (lesson_id IS NULL AND media_asset_id IS NOT NULL)
        ),
    CONSTRAINT ck_content_review_status
        CHECK (status IN ('APPROVED', 'REJECTED', 'REVISION_REQUIRED'))
);

-- =========================================================
-- 12. SYSTEM ANALYTICS
-- This is usage telemetry for active users/session duration/retention.
-- It is NOT the Redis authentication session store.
-- =========================================================
CREATE TABLE usage_sessions (
    usage_session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    platform         VARCHAR(20) NOT NULL,
    started_at       TIMESTAMPTZ NOT NULL,
    ended_at         TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_usage_platform
        CHECK (platform IN ('WINDOWS', 'ANDROID', 'IOS', 'WEB')),
    CONSTRAINT ck_usage_time CHECK (ended_at IS NULL OR ended_at >= started_at)
);

-- =========================================================
-- 13. APPLICATION CONFIGURATION
-- =========================================================
CREATE TABLE app_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key         VARCHAR(120) NOT NULL UNIQUE,
    config_value       JSONB NOT NULL,
    description        TEXT,
    updated_by_user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- UPDATED_AT TRIGGER
-- =========================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_learner_profiles_updated_at
BEFORE UPDATE ON learner_profiles
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_instructor_profiles_updated_at
BEFORE UPDATE ON instructor_profiles
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_instruments_updated_at
BEFORE UPDATE ON instruments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_techniques_updated_at
BEFORE UPDATE ON techniques
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_lessons_updated_at
BEFORE UPDATE ON lessons
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_exercises_updated_at
BEFORE UPDATE ON exercises
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_quizzes_updated_at
BEFORE UPDATE ON quizzes
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_minigames_updated_at
BEFORE UPDATE ON minigame_challenges
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_lesson_progress_updated_at
BEFORE UPDATE ON learner_lesson_progress
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_daily_progress_updated_at
BEFORE UPDATE ON learner_daily_challenges
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_app_configs_updated_at
BEFORE UPDATE ON app_configs
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =========================================================
-- INDEXES
-- =========================================================
CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_learner_instruments_instrument ON learner_instruments(instrument_id);
CREATE INDEX idx_instructor_instruments_instrument ON instructor_instruments(instrument_id);
CREATE INDEX idx_techniques_instrument ON techniques(instrument_id);
CREATE INDEX idx_lessons_instrument_level_status
    ON lessons(instrument_id, skill_level_id, status, order_index);
CREATE INDEX idx_media_assets_lesson_type ON media_assets(lesson_id, asset_type);
CREATE INDEX idx_exercises_lesson ON exercises(lesson_id);
CREATE INDEX idx_practice_attempts_learner_time
    ON practice_attempts(learner_user_id, completed_at DESC);
CREATE INDEX idx_practice_attempts_exercise ON practice_attempts(exercise_id);
CREATE INDEX idx_feedback_attempt ON instructor_feedback(attempt_id);
CREATE INDEX idx_feedback_instructor ON instructor_feedback(instructor_user_id);
CREATE INDEX idx_quizzes_lesson ON quizzes(lesson_id);
CREATE INDEX idx_quiz_attempts_learner_time
    ON quiz_attempts(learner_user_id, completed_at DESC);
CREATE INDEX idx_minigames_lesson ON minigame_challenges(lesson_id);
CREATE INDEX idx_minigame_attempts_learner_time
    ON minigame_attempts(learner_user_id, completed_at DESC);
CREATE INDEX idx_progress_lesson ON learner_lesson_progress(lesson_id);
CREATE INDEX idx_daily_challenges_date ON daily_challenges(challenge_date);
CREATE INDEX idx_content_reviews_lesson ON content_reviews(lesson_id)
    WHERE lesson_id IS NOT NULL;
CREATE INDEX idx_content_reviews_asset ON content_reviews(media_asset_id)
    WHERE media_asset_id IS NOT NULL;
CREATE INDEX idx_usage_sessions_user_start
    ON usage_sessions(user_id, started_at DESC);

-- =========================================================
-- REFERENCE SEED DATA ONLY
-- No sample credentials or hard-coded user IDs.
-- =========================================================
INSERT INTO roles (role_name) VALUES
    ('ADMIN'),
    ('INSTRUCTOR'),
    ('LEARNER')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO skill_levels (level_code, level_name, order_index) VALUES
    ('BEGINNER', 'Beginner', 1),
    ('INTERMEDIATE', 'Intermediate', 2),
    ('ADVANCED', 'Advanced', 3)
ON CONFLICT (level_code) DO NOTHING;

INSERT INTO instruments (name, description) VALUES
    ('Đàn Tranh', 'Vietnamese 16-string zither'),
    ('Đàn Bầu',   'Vietnamese monochord'),
    ('Sáo',  'Vietnamese bamboo flute'),
    ('Trống Chầu',     'Vietnamese traditional drum')
ON CONFLICT (name) DO NOTHING;

INSERT INTO app_configs (config_key, config_value, description) VALUES
    ('scoring.pitch_weight',        '0.35'::jsonb, 'Pitch contribution to the composite score'),
    ('scoring.rhythm_weight',       '0.25'::jsonb, 'Rhythm contribution to the composite score'),
    ('scoring.tonal_weight',        '0.15'::jsonb, 'Tonal-quality contribution where applicable'),
    ('scoring.breath_weight',       '0.10'::jsonb, 'Breath contribution for wind instruments'),
    ('scoring.dynamics_weight',     '0.15'::jsonb, 'Dynamics contribution to the composite score'),
    ('feature.minigame_enabled',    'true'::jsonb, 'Global mini-game feature toggle'),
    ('feature.adaptive_difficulty', 'true'::jsonb, 'Adaptive difficulty feature toggle'),
    ('difficulty.rolling_window',   '10'::jsonb, 'Number of recent attempts used for adaptation')
ON CONFLICT (config_key) DO NOTHING;

COMMIT;

-- =========================================================
-- REDIS (NOT POSTGRESQL TABLES)
-- =========================================================
-- Auth refresh sessions:
--   auth:session:{session_id}
--
-- Leaderboard cache (Sorted Set):
--   leaderboard:{scope}
--   member = learner_user_id
--   score  = ranking score calculated by application rules
    completed_at    TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (learner_user_id, challenge_id),
    CONSTRAINT ck_daily_progress_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT ck_daily_progress_value CHECK (progress_value >= 0)
);

-- =========================================================
-- 11. CONTENT MODERATION
-- Each review targets exactly one lesson OR one media asset.
-- =========================================================
CREATE TABLE content_reviews (
    id BIGSERIAL PRIMARY KEY,
    reviewer_user_id  BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    lesson_id         BIGINT REFERENCES lessons(lesson_id) ON DELETE RESTRICT,
    media_asset_id    BIGINT REFERENCES media_assets(asset_id) ON DELETE RESTRICT,
    status            VARCHAR(30) NOT NULL,
    comment           TEXT,
    reviewed_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_content_review_target
        CHECK (
            (lesson_id IS NOT NULL AND media_asset_id IS NULL)
            OR
            (lesson_id IS NULL AND media_asset_id IS NOT NULL)
        ),
    CONSTRAINT ck_content_review_status
        CHECK (status IN ('APPROVED', 'REJECTED', 'REVISION_REQUIRED'))
);

-- =========================================================
-- 12. SYSTEM ANALYTICS
-- This is usage telemetry for active users/session duration/retention.
-- It is NOT the Redis authentication session store.
-- =========================================================
CREATE TABLE usage_sessions (
    usage_session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    platform         VARCHAR(20) NOT NULL,
    started_at       TIMESTAMPTZ NOT NULL,
    ended_at         TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_usage_platform
        CHECK (platform IN ('WINDOWS', 'ANDROID', 'IOS', 'WEB')),
    CONSTRAINT ck_usage_time CHECK (ended_at IS NULL OR ended_at >= started_at)
);

-- =========================================================
-- 13. APPLICATION CONFIGURATION
-- =========================================================
CREATE TABLE app_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key         VARCHAR(120) NOT NULL UNIQUE,
    config_value       JSONB NOT NULL,
    description        TEXT,
    updated_by_user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- UPDATED_AT TRIGGER
-- =========================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_learner_profiles_updated_at
BEFORE UPDATE ON learner_profiles
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_instructor_profiles_updated_at
BEFORE UPDATE ON instructor_profiles
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_instruments_updated_at
BEFORE UPDATE ON instruments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_techniques_updated_at
BEFORE UPDATE ON techniques
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_lessons_updated_at
BEFORE UPDATE ON lessons
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_exercises_updated_at
BEFORE UPDATE ON exercises
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_quizzes_updated_at
BEFORE UPDATE ON quizzes
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_minigames_updated_at
BEFORE UPDATE ON minigame_challenges
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_lesson_progress_updated_at
BEFORE UPDATE ON learner_lesson_progress
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_daily_progress_updated_at
BEFORE UPDATE ON learner_daily_challenges
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_app_configs_updated_at
BEFORE UPDATE ON app_configs
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =========================================================
-- INDEXES
-- =========================================================
CREATE INDEX idx_users_role ON users(role_id);
CREATE INDEX idx_learner_instruments_instrument ON learner_instruments(instrument_id);
CREATE INDEX idx_instructor_instruments_instrument ON instructor_instruments(instrument_id);
CREATE INDEX idx_techniques_instrument ON techniques(instrument_id);
CREATE INDEX idx_lessons_instrument_level_status
    ON lessons(instrument_id, skill_level_id, status, order_index);
CREATE INDEX idx_media_assets_lesson_type ON media_assets(lesson_id, asset_type);
CREATE INDEX idx_exercises_lesson ON exercises(lesson_id);
CREATE INDEX idx_practice_attempts_learner_time
    ON practice_attempts(learner_user_id, completed_at DESC);
CREATE INDEX idx_practice_attempts_exercise ON practice_attempts(exercise_id);
CREATE INDEX idx_feedback_attempt ON instructor_feedback(attempt_id);
CREATE INDEX idx_feedback_instructor ON instructor_feedback(instructor_user_id);
CREATE INDEX idx_quizzes_lesson ON quizzes(lesson_id);
CREATE INDEX idx_quiz_attempts_learner_time
    ON quiz_attempts(learner_user_id, completed_at DESC);
CREATE INDEX idx_minigames_lesson ON minigame_challenges(lesson_id);
CREATE INDEX idx_minigame_attempts_learner_time
    ON minigame_attempts(learner_user_id, completed_at DESC);
CREATE INDEX idx_progress_lesson ON learner_lesson_progress(lesson_id);
CREATE INDEX idx_daily_challenges_date ON daily_challenges(challenge_date);
CREATE INDEX idx_content_reviews_lesson ON content_reviews(lesson_id)
    WHERE lesson_id IS NOT NULL;
CREATE INDEX idx_content_reviews_asset ON content_reviews(media_asset_id)
    WHERE media_asset_id IS NOT NULL;
CREATE INDEX idx_usage_sessions_user_start
    ON usage_sessions(user_id, started_at DESC);

-- =========================================================
-- REFERENCE SEED DATA ONLY
-- No sample credentials or hard-coded user IDs.
-- =========================================================
INSERT INTO roles (role_name) VALUES
    ('ADMIN'),
    ('INSTRUCTOR'),
    ('LEARNER')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO skill_levels (level_code, level_name, order_index) VALUES
    ('BEGINNER', 'Beginner', 1),
    ('INTERMEDIATE', 'Intermediate', 2),
    ('ADVANCED', 'Advanced', 3)
ON CONFLICT (level_code) DO NOTHING;

INSERT INTO instruments (name, description) VALUES
    ('Đàn Tranh', 'Vietnamese 16-string zither'),
    ('Đàn Bầu',   'Vietnamese monochord'),
    ('Sáo',  'Vietnamese bamboo flute'),
    ('Trống Chầu',     'Vietnamese traditional drum')
ON CONFLICT (name) DO NOTHING;

INSERT INTO app_configs (config_key, config_value, description) VALUES
    ('scoring.pitch_weight',        '0.35'::jsonb, 'Pitch contribution to the composite score'),
    ('scoring.rhythm_weight',       '0.25'::jsonb, 'Rhythm contribution to the composite score'),
    ('scoring.tonal_weight',        '0.15'::jsonb, 'Tonal-quality contribution where applicable'),
    ('scoring.breath_weight',       '0.10'::jsonb, 'Breath contribution for wind instruments'),
    ('scoring.dynamics_weight',     '0.15'::jsonb, 'Dynamics contribution to the composite score'),
    ('feature.minigame_enabled',    'true'::jsonb, 'Global mini-game feature toggle'),
    ('feature.adaptive_difficulty', 'true'::jsonb, 'Adaptive difficulty feature toggle'),
    ('difficulty.rolling_window',   '10'::jsonb, 'Number of recent attempts used for adaptation')
ON CONFLICT (config_key) DO NOTHING;

COMMIT;

-- =========================================================
-- REDIS (NOT POSTGRESQL TABLES)
-- =========================================================
-- Auth refresh sessions:
--   auth:session:{session_id}
--
-- Leaderboard cache (Sorted Set):
--   leaderboard:{scope}
--   member = learner_user_id
--   score  = ranking score calculated by application rules
--
-- PostgreSQL remains the source of truth for attempts, progress,
-- achievements and daily-challenge completion.
-- =========================================================

-- Seed Admin and Instructor accounts (Mật khẩu cho cả 2 là: 123456)
INSERT INTO users (email, password_hash, full_name, role_id, is_active, created_at, updated_at)
VALUES 
('VS-2024-001', 'admin@vietstage.com', '$2a$10$FPCXHRtYgKFc9yLiKREjdO88E8P2lyEbphhTLi7T/Z7OfQYtuyOim', 'System Admin', (SELECT role_id FROM roles WHERE role_name = 'ADMIN'), true, NOW(), NOW()),
('instructor@vietstage.com', '$2a$10$FPCXHRtYgKFc9yLiKREjdO88E8P2lyEbphhTLi7T/Z7OfQYtuyOim', 'Master Instructor', (SELECT role_id FROM roles WHERE role_name = 'INSTRUCTOR'), true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
