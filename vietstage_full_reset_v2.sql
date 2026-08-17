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
DROP TABLE IF EXISTS audit_logs CASCADE;
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
    avatar_url     TEXT,
    fcm_token      VARCHAR(500),
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    log_id          BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    action_type     VARCHAR(50) NOT NULL,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       VARCHAR(100),
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_logs_user_time ON audit_logs(user_id, created_at DESC);

CREATE TABLE learner_profiles (
    user_id                 BIGINT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    avatar_url              TEXT,
    total_practice_seconds  BIGINT NOT NULL DEFAULT 0,
    current_streak          INTEGER NOT NULL DEFAULT 0,
    longest_streak          INTEGER NOT NULL DEFAULT 0,
    total_points            INTEGER NOT NULL DEFAULT 0,
    total_stars             INTEGER NOT NULL DEFAULT 0,
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
    guide_url     VARCHAR(255),
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
    lesson_code          VARCHAR(50) UNIQUE,
    instrument_id        BIGINT NOT NULL REFERENCES instruments(id) ON DELETE RESTRICT,
    skill_level_id       BIGINT NOT NULL REFERENCES skill_levels(skill_level_id) ON DELETE RESTRICT,
    created_by_user_id   BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    title                VARCHAR(200) NOT NULL,
    description          TEXT,
    technical_notes      TEXT,
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
-- 14. APPLICATION CONFIGURATION
-- =========================================================
CREATE TABLE app_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key         VARCHAR(120) NOT NULL UNIQUE,
    config_value       VARCHAR(255) NOT NULL,
    config_group       VARCHAR(50),
    description        TEXT,
    value_type         VARCHAR(20),
    min_value          DOUBLE PRECISION,
    max_value          DOUBLE PRECISION,
    step_value         DOUBLE PRECISION,
    options            TEXT,
    default_value      VARCHAR(255),
    updated_by         BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
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

INSERT INTO app_configs (config_key, config_value, config_group, description, value_type, min_value, max_value, step_value, options, default_value) VALUES
    ('scoring.pitch_weight',        '0.35', 'SCORING', 'Pitch contribution to the composite score', 'NUMBER', 0.0, 1.0, 0.05, NULL, '0.35'),
    ('scoring.rhythm_weight',       '0.25', 'SCORING', 'Rhythm contribution to the composite score', 'NUMBER', 0.0, 1.0, 0.05, NULL, '0.25'),
    ('scoring.tonal_weight',        '0.15', 'SCORING', 'Tonal-quality contribution where applicable', 'NUMBER', 0.0, 1.0, 0.05, NULL, '0.15'),
    ('scoring.breath_weight',       '0.10', 'SCORING', 'Breath contribution for wind instruments', 'NUMBER', 0.0, 1.0, 0.05, NULL, '0.10'),
    ('scoring.dynamics_weight',     '0.15', 'SCORING', 'Dynamics contribution to the composite score', 'NUMBER', 0.0, 1.0, 0.05, NULL, '0.15'),
    ('feature.minigame_enabled',    'true', 'FEATURE', 'Global mini-game feature toggle', 'BOOLEAN', NULL, NULL, NULL, 'true,false', 'true'),
    ('feature.adaptive_difficulty', 'true', 'FEATURE', 'Adaptive difficulty feature toggle', 'BOOLEAN', NULL, NULL, NULL, 'true,false', 'true'),
    ('difficulty.rolling_window',   '10',   'DIFFICULTY', 'Number of recent attempts used for adaptation', 'NUMBER', 1, 100, 1, NULL, '10')
ON CONFLICT (config_key) DO NOTHING;

COMMIT;

-- =========================================================
-- MOCK DATA FOR VIETSTAGE (ADMIN, INSTRUCTOR, LEARNER, LESSONS, LOGS)
-- Password for all mock users is: 123456
-- Hash: $2a$10$FPCXHRtYgKFc9yLiKREjdO88E8P2lyEbphhTLi7T/Z7OfQYtuyOim
-- =========================================================

-- 1. USERS
INSERT INTO users (user_code, email, password_hash, full_name, role_id, avatar_url, is_active, created_at, updated_at) VALUES 
('VS-2024-001', 'admin@vietstage.com', '$2a$10$FPCXHRtYgKFc9yLiKREjdO88E8P2lyEbphhTLi7T/Z7OfQYtuyOim', 'System Admin', (SELECT role_id FROM roles WHERE role_name = 'ADMIN'), 'https://i.pravatar.cc/150?u=admin1', true, NOW() - INTERVAL '30 days', NOW()),
('VS-2024-002', 'quang@vietstage.com', '$2a$10$FPCXHRtYgKFc9yLiKREjdO88E8P2lyEbphhTLi7T/Z7OfQYtuyOim', 'Nguyễn Đăng Quang', (SELECT role_id FROM roles WHERE role_name = 'ADMIN'), 'https://i.pravatar.cc/150?u=admin2', true, NOW() - INTERVAL '20 days', NOW()),
('VS-2024-003', 'dantranh.master@vietstage.com', '$2a$10$FPCXHRtYgKFc9yLiKREjdO88E8P2lyEbphhTLi7T/Z7OfQYtuyOim', 'Trần Thị Thu Thủy', (SELECT role_id FROM roles WHERE role_name = 'INSTRUCTOR'), 'https://i.pravatar.cc/150?u=ins1', true, NOW() - INTERVAL '40 days', NOW()),
('VS-2024-004', 'saotruc.guru@vietstage.com', '$2a$10$FPCXHRtYgKFc9yLiKREjdO88E8P2lyEbphhTLi7T/Z7OfQYtuyOim', 'Lê Văn Khang', (SELECT role_id FROM roles WHERE role_name = 'INSTRUCTOR'), 'https://i.pravatar.cc/150?u=ins2', true, NOW() - INTERVAL '35 days', NOW()),
('VS-2024-005', 'danbau.pro@vietstage.com', '$2a$10$FPCXHRtYgKFc9yLiKREjdO88E8P2lyEbphhTLi7T/Z7OfQYtuyOim', 'Phạm Minh Trí', (SELECT role_id FROM roles WHERE role_name = 'INSTRUCTOR'), 'https://i.pravatar.cc/150?u=ins3', true, NOW() - INTERVAL '10 days', NOW()),
('VS-2024-006', 'learner1@gmail.com', '$2a$10$FPCXHRtYgKFc9yLiKREjdO88E8P2lyEbphhTLi7T/Z7OfQYtuyOim', 'Học Viên Một', (SELECT role_id FROM roles WHERE role_name = 'LEARNER'), 'https://i.pravatar.cc/150?u=learn1', true, NOW() - INTERVAL '5 days', NOW()),
('VS-2024-007', 'learner2@gmail.com', '$2a$10$FPCXHRtYgKFc9yLiKREjdO88E8P2lyEbphhTLi7T/Z7OfQYtuyOim', 'Học Viên Hai', (SELECT role_id FROM roles WHERE role_name = 'LEARNER'), 'https://i.pravatar.cc/150?u=learn2', true, NOW() - INTERVAL '2 days', NOW())
ON CONFLICT (email) DO NOTHING;

-- 2. INSTRUCTOR PROFILES
INSERT INTO instructor_profiles (user_id, biography, years_experience) VALUES 
((SELECT user_id FROM users WHERE email = 'dantranh.master@vietstage.com'), 'Nghệ sĩ Đàn Tranh với 15 năm kinh nghiệm biểu diễn quốc tế.', 15),
((SELECT user_id FROM users WHERE email = 'saotruc.guru@vietstage.com'), 'Thạc sĩ âm nhạc truyền thống, chuyên ngành Sáo Trúc.', 10),
((SELECT user_id FROM users WHERE email = 'danbau.pro@vietstage.com'), 'Chuyên gia Đàn Bầu, từng đạt giải nhất Tiếng Hát Truyền Hình.', 8)
ON CONFLICT (user_id) DO NOTHING;

-- 3. INSTRUCTOR INSTRUMENTS (Specialty)
INSERT INTO instructor_instruments (instructor_user_id, instrument_id) VALUES 
((SELECT user_id FROM users WHERE email = 'dantranh.master@vietstage.com'), (SELECT instrument_id FROM instruments WHERE name = 'Đàn Tranh')),
((SELECT user_id FROM users WHERE email = 'saotruc.guru@vietstage.com'), (SELECT instrument_id FROM instruments WHERE name = 'Sáo')),
((SELECT user_id FROM users WHERE email = 'danbau.pro@vietstage.com'), (SELECT instrument_id FROM instruments WHERE name = 'Đàn Bầu')),
((SELECT user_id FROM users WHERE email = 'danbau.pro@vietstage.com'), (SELECT instrument_id FROM instruments WHERE name = 'Đàn Tranh'))
ON CONFLICT DO NOTHING;

-- 4. LEARNER PROFILES
INSERT INTO learner_profiles (user_id, total_practice_seconds, current_streak, longest_streak) VALUES 
((SELECT user_id FROM users WHERE email = 'learner1@gmail.com'), 3600, 2, 5),
((SELECT user_id FROM users WHERE email = 'learner2@gmail.com'), 7200, 4, 10)
ON CONFLICT (user_id) DO NOTHING;

-- 5. LESSONS
INSERT INTO lessons (instrument_id, skill_level_id, created_by_user_id, title, description, status, order_index) VALUES 
((SELECT instrument_id FROM instruments WHERE name = 'Đàn Tranh'), (SELECT skill_level_id FROM skill_levels WHERE level_code = 'BEGINNER'), (SELECT user_id FROM users WHERE email = 'dantranh.master@vietstage.com'), 'Bài 1: Làm quen Đàn Tranh', 'Cách gảy cơ bản', 'APPROVED', 1),
((SELECT instrument_id FROM instruments WHERE name = 'Đàn Tranh'), (SELECT skill_level_id FROM skill_levels WHERE level_code = 'BEGINNER'), (SELECT user_id FROM users WHERE email = 'dantranh.master@vietstage.com'), 'Bài 2: Nốt nhạc cơ bản', 'Các nốt trên Đàn Tranh', 'APPROVED', 2),
((SELECT instrument_id FROM instruments WHERE name = 'Sáo'), (SELECT skill_level_id FROM skill_levels WHERE level_code = 'BEGINNER'), (SELECT user_id FROM users WHERE email = 'saotruc.guru@vietstage.com'), 'Bài 1: Cách thổi Sáo', 'Cách lấy hơi và thổi', 'APPROVED', 1)
ON CONFLICT DO NOTHING;

-- 6. EXERCISES
INSERT INTO exercises (lesson_id, title, description, order_index) VALUES 
((SELECT lesson_id FROM lessons WHERE title = 'Bài 1: Làm quen Đàn Tranh'), 'Thực hành gảy Đàn Tranh 1', 'Gảy các nốt Đồ Rê Mi', 1),
((SELECT lesson_id FROM lessons WHERE title = 'Bài 2: Nốt nhạc cơ bản'), 'Thực hành gảy Đàn Tranh 2', 'Bài Lý Cây Bông', 1),
((SELECT lesson_id FROM lessons WHERE title = 'Bài 1: Cách thổi Sáo'), 'Thực hành thổi Sáo 1', 'Luyện tập lấy hơi', 1)
ON CONFLICT DO NOTHING;

-- 7. AUDIT LOGS (Activities)
INSERT INTO audit_logs (user_id, action_type, entity_type, entity_id, description, created_at) VALUES 
((SELECT user_id FROM users WHERE email = 'dantranh.master@vietstage.com'), 'CREATE_LESSON', 'LESSON', '1', 'Đăng tải bài giảng: Bài 1 Làm quen Đàn Tranh', NOW() - INTERVAL '5 hours'),
((SELECT user_id FROM users WHERE email = 'admin@vietstage.com'), 'APPROVE_LESSON', 'LESSON', '1', 'Phê duyệt bài giảng của giảng viên Trần Thị Thu Thủy', NOW() - INTERVAL '4 hours'),
((SELECT user_id FROM users WHERE email = 'saotruc.guru@vietstage.com'), 'UPDATE_PROFILE', 'USER', '4', 'Cập nhật ảnh đại diện mới', NOW() - INTERVAL '1 day'),
((SELECT user_id FROM users WHERE email = 'quang@vietstage.com'), 'CREATE_USER', 'USER', '3', 'Tạo tài khoản cho giảng viên Trần Thị Thu Thủy', NOW() - INTERVAL '10 days');

-- MOCK DATA FOR METRICS (Dashboard)
INSERT INTO usage_sessions (user_id, session_token, started_at, last_active_at, ip_address, user_agent, is_active) VALUES
(3, 'tok_1', '2026-08-07T07:54:38.807Z', '2026-08-07T07:54:38.807Z', '192.168.1.1', 'Mozilla', true),
(3, 'tok_2', '2026-08-06T07:54:38.812Z', '2026-08-06T07:54:38.812Z', '192.168.1.1', 'Mozilla', true),
(3, 'tok_3', '2026-08-05T07:54:38.812Z', '2026-08-05T07:54:38.812Z', '192.168.1.1', 'Mozilla', true),
(3, 'tok_4', '2026-08-04T07:54:38.812Z', '2026-08-04T07:54:38.812Z', '192.168.1.1', 'Mozilla', true),
(3, 'tok_5', '2026-08-03T07:54:38.812Z', '2026-08-03T07:54:38.812Z', '192.168.1.1', 'Mozilla', true),
(3, 'tok_6', '2026-08-02T07:54:38.812Z', '2026-08-02T07:54:38.812Z', '192.168.1.1', 'Mozilla', true),
(3, 'tok_7', '2026-08-01T07:54:38.812Z', '2026-08-01T07:54:38.812Z', '192.168.1.1', 'Mozilla', true),
(3, 'tok_8', '2026-07-31T07:54:38.812Z', '2026-07-31T07:54:38.812Z', '192.168.1.1', 'Mozilla', true),
(3, 'tok_9', '2026-07-30T07:54:38.812Z', '2026-07-30T07:54:38.812Z', '192.168.1.1', 'Mozilla', true),
(3, 'tok_10', '2026-07-29T07:54:38.812Z', '2026-07-29T07:54:38.812Z', '192.168.1.1', 'Mozilla', true);

INSERT INTO practice_sessions (learner_id, started_at, ended_at, duration_minutes) VALUES
(3, '2026-08-07T07:54:38.812Z', '2026-08-07T07:54:38.812Z', 11),
(3, '2026-08-06T07:54:38.812Z', '2026-08-06T07:54:38.812Z', 12),
(3, '2026-08-05T07:54:38.812Z', '2026-08-05T07:54:38.812Z', 13),
(3, '2026-08-04T07:54:38.812Z', '2026-08-04T07:54:38.812Z', 14),
(3, '2026-08-03T07:54:38.812Z', '2026-08-03T07:54:38.812Z', 15),
(3, '2026-08-02T07:54:38.812Z', '2026-08-02T07:54:38.812Z', 16),
(3, '2026-08-01T07:54:38.812Z', '2026-08-01T07:54:38.812Z', 17),
(3, '2026-07-31T07:54:38.812Z', '2026-07-31T07:54:38.812Z', 18),
(3, '2026-07-30T07:54:38.812Z', '2026-07-30T07:54:38.812Z', 19),
(3, '2026-07-29T07:54:38.812Z', '2026-07-29T07:54:38.812Z', 20);

INSERT INTO practice_attempts (session_id, learner_id, exercise_id, pitch_score, rhythm_score, dynamics_score, breath_score, tonal_quality_score, total_score, stars, points_earned, sync_status) VALUES
(1, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED'),
(2, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED'),
(3, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED'),
(4, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED'),
(5, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED'),
(6, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED'),
(7, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED'),
(8, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED'),
(9, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED'),
(10, 3, 1, 80, 80, 80, 80, 80, 80, 3, 10, 'SYNCED');
