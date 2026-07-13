-- =========================================================
-- VIETSTAGE POSTGRESQL SCRIPT v3.0
-- Cập nhật từ v2.2, theo conceptual ERD đã chốt trong buổi review:
--   [1] Thêm bảng `roles` riêng, users.role_id thay cho cột enum `role`
--   [2] Tách `quizzes` / `quiz_attempts` độc lập (KHÔNG gộp vào mini-game)
--       Lesson (1) — Quiz (N)
--   [3] Gộp rhythm_match + melody_complete -> `minigame_challenges`
--       (challenge_type), Lesson (1) — Minigame_Challenge (N)
--       SỬA sau review giảng viên: ERD phải TĨNH, cardinality phản ánh
--       khả năng cấu trúc dữ liệu, không phải hành vi vận hành hiện tại
--   [4] Bỏ mini_games / lesson_mini_games / mini_game_results (thiết kế cũ)
--   [5] Bỏ learner_progress (không còn trong conceptual ERD mới)
--   [6] KHÔI PHỤC lesson_completions (bản rút gọn learner_progress cũ)
--       sau khi phát hiện cosmetic_items.unlock_type='STARS' không có
--       nguồn dữ liệu nào — "stars theo lesson" không suy ra được từ
--       3 bảng attempt hiện có
--   [7] Thêm instruments.is_active — thay cho DELETE (tránh cascade),
--       theo đúng pattern soft-disable của users
-- =========================================================

-- =========================================================
-- DROP TABLES (theo thứ tự dependency)
-- =========================================================
DROP TABLE IF EXISTS app_configs CASCADE;
DROP TABLE IF EXISTS leaderboards CASCADE;
DROP TABLE IF EXISTS point_transactions CASCADE;
DROP TABLE IF EXISTS learner_daily_challenges CASCADE;
DROP TABLE IF EXISTS daily_challenges CASCADE;
DROP TABLE IF EXISTS learner_cosmetics CASCADE;
DROP TABLE IF EXISTS cosmetic_items CASCADE;
DROP TABLE IF EXISTS learner_achievements CASCADE;
DROP TABLE IF EXISTS achievements CASCADE;
DROP TABLE IF EXISTS minigame_attempts CASCADE;
DROP TABLE IF EXISTS minigame_challenges CASCADE;
DROP TABLE IF EXISTS quiz_attempts CASCADE;
DROP TABLE IF EXISTS quizzes CASCADE;
DROP TABLE IF EXISTS instructor_feedback CASCADE;
DROP TABLE IF EXISTS practice_attempts CASCADE;
DROP TABLE IF EXISTS practice_sessions CASCADE;
DROP TABLE IF EXISTS exercises CASCADE;
DROP TABLE IF EXISTS content_reviews CASCADE;
DROP TABLE IF EXISTS lesson_assets CASCADE;
DROP TABLE IF EXISTS lesson_contents CASCADE;
DROP TABLE IF EXISTS lesson_techniques CASCADE;
DROP TABLE IF EXISTS lesson_completions CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS techniques CASCADE;
DROP TABLE IF EXISTS instruments CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS learner_profiles CASCADE;
DROP TABLE IF EXISTS instructor_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS skill_levels CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
-- Bảng cũ không còn dùng
DROP TABLE IF EXISTS mini_games CASCADE;
DROP TABLE IF EXISTS lesson_mini_games CASCADE;
DROP TABLE IF EXISTS mini_game_results CASCADE;
DROP TABLE IF EXISTS learner_progress CASCADE;
DROP TABLE IF EXISTS instructor_requests CASCADE;
DROP TABLE IF EXISTS learner_feedback CASCADE;


-- =========================================================
-- 1. Vai trò người dùng — tách bảng riêng
-- =========================================================
CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL  -- ADMIN | INSTRUCTOR | LEARNER
);


-- =========================================================
-- 2. Tài khoản người dùng core
-- =========================================================
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    email         VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    is_active     BOOLEAN   DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- 3. Trình độ học viên
-- =========================================================
CREATE TABLE skill_levels (
    id         BIGSERIAL PRIMARY KEY,
    level_name VARCHAR(50) UNIQUE NOT NULL  -- BEGINNER | INTERMEDIATE | ADVANCED
);


-- =========================================================
-- 4. Hồ sơ Học viên / Giảng viên
-- =========================================================
CREATE TABLE learner_profiles (
    user_id                 BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    skill_level_id          BIGINT REFERENCES skill_levels(id) ON DELETE SET NULL,
    favorite_instrument     VARCHAR(100),
    total_practice_minutes  INT DEFAULT 0,
    current_streak          INT DEFAULT 0,
    longest_streak          INT DEFAULT 0,
    last_practice_date      DATE,
    adaptive_difficulty     VARCHAR(20) DEFAULT 'BEGINNER'
                               CHECK (adaptive_difficulty IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE instructor_profiles (
    user_id          BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    specialization   VARCHAR(200),
    biography        TEXT,
    years_experience INT DEFAULT 0,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- 5. Hệ thống thông báo
-- =========================================================
CREATE TABLE notifications (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title             VARCHAR(200) NOT NULL,
    message           TEXT NOT NULL,
    notification_type VARCHAR(50), -- SYSTEM | ACHIEVEMENT | STREAK | FEEDBACK | CHALLENGE
    is_read           BOOLEAN DEFAULT FALSE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- 6. Nhạc cụ & Kỹ thuật đặc thù
-- =========================================================
CREATE TABLE instruments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon_url    TEXT,
    is_active   BOOLEAN DEFAULT TRUE  -- soft-disable thay vì DELETE (tránh cascade phá huỷ lessons/techniques)
);

CREATE TABLE techniques (
    id            BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT REFERENCES instruments(id) ON DELETE CASCADE,
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    guide_url     TEXT
);


-- =========================================================
-- 7. Bài học giáo trình
-- =========================================================
CREATE TABLE lessons (
    id             BIGSERIAL PRIMARY KEY,
    instrument_id  BIGINT REFERENCES instruments(id) ON DELETE CASCADE,
    skill_level_id BIGINT REFERENCES skill_levels(id) ON DELETE SET NULL,
    title          VARCHAR(200) NOT NULL,
    description    TEXT,
    status         VARCHAR(20) DEFAULT 'DRAFT'
                       CHECK (status IN ('DRAFT','PENDING','APPROVED','REJECTED')),
    order_index    INT DEFAULT 0,
    created_by     BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lesson_techniques (
    lesson_id    BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    technique_id BIGINT REFERENCES techniques(id) ON DELETE CASCADE,
    PRIMARY KEY (lesson_id, technique_id)
);

-- =========================================================
-- 7b. Lesson Completions — KHÔI PHỤC sau khi phát hiện gap: bảng cũ
--     learner_progress bị xoá ("ERD phải tĩnh") nhưng không có gì thay
--     thế khái niệm "stars theo lesson", trong khi
--     cosmetic_items.unlock_type = 'STARS' phụ thuộc trực tiếp vào nó.
--     Đây là bảng TỔNG HỢP kết quả (không phải nguồn dữ liệu gốc) —
--     stars được server tính và upsert vào đây mỗi khi 1 practice_attempt
--     mới trong lesson đó đạt/vượt pass_threshold của exercise.
-- =========================================================
CREATE TABLE lesson_completions (
    learner_id   BIGINT REFERENCES users(id) ON DELETE CASCADE,
    lesson_id    BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    stars        INT DEFAULT 0 CHECK (stars BETWEEN 0 AND 3),
    completed    BOOLEAN DEFAULT FALSE,
    started_at   TIMESTAMP,
    completed_at TIMESTAMP,
    sync_status  VARCHAR(20) DEFAULT 'SYNCED'
                     CHECK (sync_status IN ('SYNCED','PENDING_SYNC','CONFLICT')),
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (learner_id, lesson_id)
);

CREATE TABLE lesson_contents (
    id           BIGSERIAL PRIMARY KEY,
    lesson_id    BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    content_text TEXT NOT NULL,
    order_index  INT DEFAULT 0
);

CREATE TABLE lesson_assets (
    id           BIGSERIAL PRIMARY KEY,
    lesson_id    BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    asset_type   VARCHAR(30) NOT NULL
                     CHECK (asset_type IN ('REFERENCE_AUDIO','SHEET_IMAGE','TECHNIQUE_VIDEO','BEAT_MAP')),
    asset_url    TEXT NOT NULL,
    tempo_bpm    INT,
    duration_sec NUMERIC(7,2),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE content_reviews (
    id          BIGSERIAL PRIMARY KEY,
    lesson_id   BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    reviewer_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    status      VARCHAR(20) NOT NULL CHECK (status IN ('APPROVED','REJECTED')),
    comment     TEXT,
    reviewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- 8. Exercise & Practice (luyện tập chính khóa, chấm AI)
-- =========================================================
CREATE TABLE exercises (
    id                BIGSERIAL PRIMARY KEY,
    lesson_id         BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    title             VARCHAR(200) NOT NULL,
    description       TEXT,
    beat_map_asset_id BIGINT REFERENCES lesson_assets(id) ON DELETE SET NULL,
    pass_threshold    NUMERIC(5,2) DEFAULT 60.00,
    order_index       INT DEFAULT 0
);

CREATE TABLE practice_sessions (
    id          BIGSERIAL PRIMARY KEY,
    learner_id  BIGINT REFERENCES users(id) ON DELETE CASCADE,
    started_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at    TIMESTAMP,
    sync_status VARCHAR(20) DEFAULT 'SYNCED'
                    CHECK (sync_status IN ('SYNCED','PENDING_SYNC','CONFLICT'))
);

CREATE TABLE practice_attempts (
    id                   BIGSERIAL PRIMARY KEY,
    session_id           BIGINT REFERENCES practice_sessions(id) ON DELETE SET NULL,
    learner_id           BIGINT REFERENCES users(id) ON DELETE CASCADE,
    exercise_id          BIGINT REFERENCES exercises(id) ON DELETE CASCADE,
    pitch_score          NUMERIC(5,2),
    rhythm_score         NUMERIC(5,2),
    dynamics_score       NUMERIC(5,2),
    tonal_quality_score  NUMERIC(5,2),
    breath_score         NUMERIC(5,2),
    total_score          NUMERIC(5,2),
    sync_status          VARCHAR(20) DEFAULT 'SYNCED'
                             CHECK (sync_status IN ('SYNCED','PENDING_SYNC','CONFLICT')),
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE instructor_feedback (
    id            BIGSERIAL PRIMARY KEY,
    attempt_id    BIGINT REFERENCES practice_attempts(id) ON DELETE CASCADE,
    instructor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    comment       TEXT NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- 9. Quiz — tách riêng, KHÔNG gộp vào mini-game
--    Lesson (1) — Quiz (N): mỗi lesson có nhiều câu hỏi
-- =========================================================
CREATE TABLE quizzes (
    id             BIGSERIAL PRIMARY KEY,
    lesson_id      BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    question       TEXT NOT NULL,
    options        TEXT NOT NULL,  -- JSON array các lựa chọn
    correct_answer TEXT NOT NULL,
    order_index    INT DEFAULT 0,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quiz_attempts (
    id              BIGSERIAL PRIMARY KEY,
    learner_id      BIGINT REFERENCES users(id) ON DELETE CASCADE,
    quiz_id         BIGINT REFERENCES quizzes(id) ON DELETE CASCADE,
    selected_answer TEXT,
    is_correct      BOOLEAN,
    score           NUMERIC(5,2),
    attempted_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- 10. Mini-game — gộp RHYTHM_MATCH + MELODY_COMPLETE
--     Lesson (1) — Minigame_Challenge (N): giống Quiz/Exercise, một
--     lesson có thể có nhiều minigame về mặt cấu trúc dữ liệu. Việc
--     "chỉ hiển thị 1 checkpoint mỗi lượt chơi" là luật nghiệp vụ ở
--     tầng ứng dụng, không phải ràng buộc ERD (không dùng UNIQUE)
-- =========================================================
CREATE TABLE minigame_challenges (
    id                 BIGSERIAL PRIMARY KEY,
    lesson_id          BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    title              VARCHAR(200) NOT NULL,
    challenge_type     VARCHAR(30) NOT NULL
                           CHECK (challenge_type IN ('RHYTHM_MATCH','MELODY_COMPLETE')),
    reference_asset_id BIGINT REFERENCES lesson_assets(id) ON DELETE SET NULL, -- BEAT_MAP khi RHYTHM_MATCH
    content_json       TEXT,  -- note_sequence + missing_positions khi MELODY_COMPLETE
    difficulty         VARCHAR(20) DEFAULT 'EASY' CHECK (difficulty IN ('EASY','MEDIUM','HARD')),
    max_score          INT DEFAULT 100,
    order_index        INT DEFAULT 0,  -- thứ tự nếu 1 lesson có nhiều minigame
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE minigame_attempts (
    id                    BIGSERIAL PRIMARY KEY,
    learner_id            BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    minigame_challenge_id BIGINT NOT NULL REFERENCES minigame_challenges(id) ON DELETE CASCADE,
    score                 INT DEFAULT 0,
    stars_earned          INT CHECK (stars_earned BETWEEN 0 AND 3),
    started_at            TIMESTAMP,
    completed_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sync_status           VARCHAR(20) DEFAULT 'SYNCED'
                              CHECK (sync_status IN ('SYNCED','PENDING_SYNC','CONFLICT'))
);


-- =========================================================
-- 11. Thành tựu (Achievements)
-- =========================================================
CREATE TABLE achievements (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    icon_url       TEXT,
    condition_json TEXT  -- vd: {"type":"STREAK_DAYS","threshold":7}
);

CREATE TABLE learner_achievements (
    learner_id     BIGINT REFERENCES users(id) ON DELETE CASCADE,
    achievement_id BIGINT REFERENCES achievements(id) ON DELETE CASCADE,
    earned_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (learner_id, achievement_id)
);


-- =========================================================
-- 12. Cosmetic Items — Virtual Room Customization
-- =========================================================
CREATE TABLE cosmetic_items (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    item_type    VARCHAR(50) NOT NULL, -- ROOM_DECOR | AVATAR_SKIN | INSTRUMENT_SKIN
    asset_url    TEXT,
    unlock_type  VARCHAR(30) CHECK (unlock_type IN ('ACHIEVEMENT','STARS','POINTS','DEFAULT')),
    unlock_value INT DEFAULT 0
);

CREATE TABLE learner_cosmetics (
    learner_id       BIGINT REFERENCES users(id) ON DELETE CASCADE,
    cosmetic_item_id BIGINT REFERENCES cosmetic_items(id) ON DELETE CASCADE,
    unlocked_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_equipped      BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (learner_id, cosmetic_item_id)
);


-- =========================================================
-- 13. Thử thách hằng ngày (Daily Challenges)
-- =========================================================
CREATE TABLE daily_challenges (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(200) NOT NULL,
    description    TEXT,
    instrument_id  BIGINT REFERENCES instruments(id) ON DELETE SET NULL,
    reward_points  INT DEFAULT 10,
    challenge_date DATE DEFAULT CURRENT_DATE
);

CREATE TABLE learner_daily_challenges (
    learner_id   BIGINT REFERENCES users(id) ON DELETE CASCADE,
    challenge_id BIGINT REFERENCES daily_challenges(id) ON DELETE CASCADE,
    completed    BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    PRIMARY KEY (learner_id, challenge_id)
);


-- =========================================================
-- 14. Hệ thống điểm số & Xếp hạng
-- =========================================================
CREATE TABLE point_transactions (
    id          BIGSERIAL PRIMARY KEY,
    learner_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_type VARCHAR(30) NOT NULL, -- PRACTICE_ATTEMPT | QUIZ_ATTEMPT | MINIGAME_ATTEMPT | ACHIEVEMENT | DAILY_CHALLENGE | STREAK_BONUS
    source_id   BIGINT,
    points      INT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leaderboards (
    learner_id   BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    total_points INT DEFAULT 0,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- 15. Cấu hình hệ thống
--     SỬA LẠI: hệ thống giờ hỗ trợ nhiều admin (endpoint
--     /api/admin/create-admin) — khôi phục updated_by vì giờ có giá trị
--     audit thật (biết đúng admin nào đã sửa config)
-- =========================================================
CREATE TABLE app_configs (
    id           BIGSERIAL PRIMARY KEY,
    config_key   VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description  TEXT,
    updated_by   BIGINT REFERENCES users(id) ON DELETE SET NULL,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- INDEX TỐI ƯU TRUY VẤN
-- =========================================================
CREATE INDEX idx_users_role                 ON users(role_id);
CREATE INDEX idx_users_email                ON users(email);

CREATE INDEX idx_lessons_instrument         ON lessons(instrument_id);
CREATE INDEX idx_lessons_skill_level        ON lessons(skill_level_id);
CREATE INDEX idx_lessons_status             ON lessons(status);

CREATE INDEX idx_exercises_lesson           ON exercises(lesson_id);
CREATE INDEX idx_lesson_completions_learner ON lesson_completions(learner_id);
CREATE INDEX idx_lesson_completions_lesson  ON lesson_completions(lesson_id);
CREATE INDEX idx_lesson_assets_lesson       ON lesson_assets(lesson_id);
CREATE INDEX idx_lesson_assets_type         ON lesson_assets(asset_type);

CREATE INDEX idx_sessions_learner           ON practice_sessions(learner_id);
CREATE INDEX idx_attempts_learner           ON practice_attempts(learner_id);
CREATE INDEX idx_attempts_exercise          ON practice_attempts(exercise_id);
CREATE INDEX idx_attempts_session           ON practice_attempts(session_id);

CREATE INDEX idx_quizzes_lesson             ON quizzes(lesson_id);
CREATE INDEX idx_quiz_attempts_learner      ON quiz_attempts(learner_id);
CREATE INDEX idx_quiz_attempts_quiz         ON quiz_attempts(quiz_id);

CREATE INDEX idx_minigame_challenges_lesson ON minigame_challenges(lesson_id);
CREATE INDEX idx_minigame_attempts_learner  ON minigame_attempts(learner_id);
CREATE INDEX idx_minigame_attempts_challenge ON minigame_attempts(minigame_challenge_id);

CREATE INDEX idx_progress_sync              ON practice_attempts(sync_status)
    WHERE sync_status != 'SYNCED';

CREATE INDEX idx_notifications_user         ON notifications(user_id);
CREATE INDEX idx_notifications_unread       ON notifications(user_id)
    WHERE is_read = FALSE;

CREATE INDEX idx_leaderboards_points        ON leaderboards(total_points DESC);
CREATE INDEX idx_point_tx_learner           ON point_transactions(learner_id);
CREATE INDEX idx_daily_challenge_date       ON daily_challenges(challenge_date);


-- =========================================================
-- SEED DATA
-- =========================================================
INSERT INTO roles (name) VALUES ('ADMIN'), ('INSTRUCTOR'), ('LEARNER');

INSERT INTO skill_levels (level_name) VALUES
    ('BEGINNER'), ('INTERMEDIATE'), ('ADVANCED');

-- Password thực tế: Admin@123 / Instruc@123 / Learner@123
INSERT INTO users (role_id, email, password_hash, full_name, is_active) VALUES
((SELECT id FROM roles WHERE name = 'ADMIN'),
 'admin@vietstage.com',
 '$2a$10$Ep6jP15pbui3U5SqytWFYOI/8Rzf76NbZwA91DZM51H2FL5FlQOsS',
 'System Admin', TRUE),
((SELECT id FROM roles WHERE name = 'INSTRUCTOR'),
 'instructor@vietstage.com',
 '$2a$10$QyeuzCZ6qKim/jEmk83fBeWx3R3Yfvs3zkbCCUSOgnvIDhIZi/f4S',
 'Nguyen Van A', TRUE),
((SELECT id FROM roles WHERE name = 'LEARNER'),
 'learner@vietstage.com',
 '$2a$10$T5WLoeD2gI9WvBLpF4wMOOlJhrsdgaShmNa.su5yjf8MG0s32EK.S',
 'Tran Thi B', TRUE),
-- Admin thứ 2 — minh hoạ hệ thống hỗ trợ nhiều admin (tạo qua /api/admin/create-admin)
((SELECT id FROM roles WHERE name = 'ADMIN'),
 'admin2@vietstage.com',
 '$2a$10$Ep6jP15pbui3U5SqytWFYOI/8Rzf76NbZwA91DZM51H2FL5FlQOsS',
 'Le Thi Quan Tri', TRUE);

INSERT INTO instructor_profiles (user_id, specialization, biography, years_experience)
VALUES (2, 'Dan Tranh', 'Traditional music instructor specializing in Vietnamese zither.', 8);

INSERT INTO learner_profiles (user_id, skill_level_id, favorite_instrument)
VALUES (3, (SELECT id FROM skill_levels WHERE level_name = 'BEGINNER'), 'Dan Tranh');

INSERT INTO instruments (name, description, icon_url) VALUES
('Dan Tranh', '16-string Vietnamese zither', 'https://assets.vietstage.com/icons/dan-tranh.png'),
('Dan Bau',   'Vietnamese monochord',        'https://assets.vietstage.com/icons/dan-bau.png'),
('Sao Truc',  'Vietnamese bamboo flute',     'https://assets.vietstage.com/icons/sao-truc.png'),
('Trong',     'Vietnamese traditional drum', 'https://assets.vietstage.com/icons/trong.png');

INSERT INTO lessons (instrument_id, skill_level_id, title, description, status, order_index, created_by) VALUES
(1, 1, 'Introduction to Dan Tranh', 'Basic posture and string identification', 'APPROVED', 1, 2),
(1, 1, 'Basic Picking Patterns',    'Fundamental picking techniques',          'APPROVED', 2, 2),
(3, 1, 'Sao Truc Breathing Basics', 'Breath control fundamentals',             'APPROVED', 1, 2);

INSERT INTO lesson_assets (lesson_id, asset_type, asset_url, tempo_bpm, duration_sec) VALUES
(1, 'REFERENCE_AUDIO', 'https://assets.vietstage.com/audio/dan-tranh-intro.mp3',      60, 45.0),
(2, 'REFERENCE_AUDIO', 'https://assets.vietstage.com/audio/picking-pattern-1.mp3',    72, 30.0),
(2, 'BEAT_MAP',        'https://assets.vietstage.com/beatmaps/picking-pattern-1.json', 72, 30.0),
(3, 'REFERENCE_AUDIO', 'https://assets.vietstage.com/audio/sao-truc-intro.mp3',       50, 60.0);

INSERT INTO exercises (lesson_id, title, description, pass_threshold, order_index) VALUES
(1, 'String Identification Quiz', 'Identify each string by its pitch',            60.00, 1),
(2, 'Picking Pattern Practice',   'Repeat the demonstrated picking sequence',      70.00, 1),
(3, 'Long Tone Exercise',         'Hold a single note for 4 beats with even tone', 65.00, 1);

-- Quiz: 1 lesson có N câu hỏi (order_index)
INSERT INTO quizzes (lesson_id, question, options, correct_answer, order_index) VALUES
(1, 'Dan Tranh has how many strings?', '["12","16","18","21"]', '16', 1),
(1, 'Which hand plucks the strings on Dan Tranh?', '["Left","Right","Both"]', 'Right', 2),
(3, 'Sao Truc is made primarily from which material?', '["Wood","Bamboo","Metal"]', 'Bamboo', 1);

-- Minigame: Lesson (1) — Minigame_Challenge (N), ví dụ lesson 1 có 2 minigame
-- để minh hoạ đúng cấu trúc 1-N (không còn ép UNIQUE lesson_id)
INSERT INTO minigame_challenges (lesson_id, title, challenge_type, reference_asset_id, difficulty, max_score, order_index) VALUES
(2, 'Picking Pattern Rhythm Match', 'RHYTHM_MATCH', 3, 'EASY', 100, 1);

INSERT INTO minigame_challenges (lesson_id, title, challenge_type, content_json, difficulty, max_score, order_index) VALUES
(1, 'Complete the Melody', 'MELODY_COMPLETE',
 '{"note_sequence":["C4","D4","E4","F4","G4"],"missing_positions":[2,4]}', 'MEDIUM', 150, 1),
(1, 'String Rhythm Warm-up', 'RHYTHM_MATCH',
 NULL, 'EASY', 100, 2);

INSERT INTO achievements (name, description, condition_json) VALUES
('First Lesson',  'Complete your very first exercise',   '{"type":"ATTEMPTS_COMPLETED","threshold":1}'),
('Hat-trick',     'Earn 3 stars on any lesson',           '{"type":"STARS_EARNED","threshold":3}'),
('Week Warrior',  'Maintain a 7-day practice streak',     '{"type":"STREAK_DAYS","threshold":7}'),
('Explorer',      'Try all 4 instruments at least once',  '{"type":"INSTRUMENTS_TRIED","threshold":4}');

INSERT INTO cosmetic_items (name, item_type, unlock_type, unlock_value) VALUES
('Bamboo Wallpaper', 'ROOM_DECOR',      'STARS',       10),
('Red Lantern Set',  'ROOM_DECOR',      'ACHIEVEMENT',  0),
('Golden Dan Tranh', 'INSTRUMENT_SKIN', 'POINTS',     500),
('Default Avatar',   'AVATAR_SKIN',     'DEFAULT',      0);

INSERT INTO daily_challenges (title, description, instrument_id, reward_points, challenge_date) VALUES
('Practice 10 Minutes on Dan Tranh', 'Complete any Dan Tranh exercise for at least 10 minutes', 1, 15, CURRENT_DATE),
('3-Star Run', 'Earn 3 stars on any lesson today', NULL, 20, CURRENT_DATE);

INSERT INTO app_configs (config_key, config_value, description, updated_by) VALUES
('scoring.pitch_weight',        '0.35', 'Weight of pitch score in composite total',           1),
('scoring.rhythm_weight',       '0.25', 'Weight of rhythm score in composite total',          1),
('scoring.dynamics_weight',     '0.15', 'Weight of dynamics score in composite total',        1),
('scoring.tonal_weight',        '0.15', 'Weight of tonal quality score (string instruments)', 1),
('scoring.breath_weight',       '0.10', 'Weight of breath score (wind instruments only)',     4),
('feature.minigame_enabled',    'true', 'Toggle mini-game feature globally',                  4),
('feature.adaptive_difficulty', 'true', 'Enable adaptive difficulty adjustment',              1),
('difficulty.adaptive_window',  '10',   'Number of recent attempts for adaptive difficulty',  1),
('offline.max_cached_lessons',  '20',   'Maximum lessons cached for offline play',            4);

INSERT INTO notifications (user_id, title, message, notification_type) VALUES
(3, 'Welcome to VietStage!',
    'Start your journey with Vietnamese traditional instruments. Your first lesson is ready!',
    'SYSTEM');

-- Practice sessions & attempts
INSERT INTO practice_sessions (learner_id, started_at, ended_at) VALUES
(3, '2026-06-10 08:00:00', '2026-06-10 08:25:00'),
(3, '2026-06-12 19:00:00', '2026-06-12 19:30:00');

INSERT INTO practice_attempts
    (session_id, learner_id, exercise_id, pitch_score, rhythm_score, dynamics_score, tonal_quality_score, total_score) VALUES
(1, 3, 1, 75.50, 70.00, 68.00, 72.00, 71.50),
(1, 3, 1, 82.00, 78.50, 75.00, 80.00, 79.10),
(2, 3, 2, 65.00, 60.00, 62.00, 64.00, 62.90);

INSERT INTO instructor_feedback (attempt_id, instructor_id, comment) VALUES
(2, 2, 'Great improvement on string identification! Keep practicing the lower strings.');

-- Lesson completions — tổng hợp từ practice_attempts ở trên (best attempt mỗi lesson)
-- Quy đổi điểm -> sao: >=90 -> 3 sao | >=75 -> 2 sao | >= pass_threshold -> 1 sao | < pass_threshold -> 0 (chưa qua)
INSERT INTO lesson_completions (learner_id, lesson_id, stars, completed, started_at, completed_at) VALUES
(3, 1, 2, TRUE,  '2026-06-10 08:00:00', '2026-06-10 08:25:00'),  -- exercise1 best=79.10 (>=75) -> 2 sao
(3, 2, 0, FALSE, '2026-06-12 19:00:00', NULL);                    -- exercise2 best=62.90 < pass_threshold(70) -> chưa qua

-- Quiz attempts
INSERT INTO quiz_attempts (learner_id, quiz_id, selected_answer, is_correct, score) VALUES
(3, 1, '16', TRUE, 100.00),
(3, 2, 'Right', TRUE, 100.00),
(3, 3, 'Bamboo', TRUE, 100.00);

-- Minigame attempts
INSERT INTO minigame_attempts (learner_id, minigame_challenge_id, score, stars_earned, started_at, completed_at) VALUES
(3, 1, 85, 3, '2026-06-12 19:25:00', '2026-06-12 19:28:00');

-- Learner achievements
INSERT INTO learner_achievements (learner_id, achievement_id) VALUES
(3, 1), (3, 2);

-- Learner cosmetics
INSERT INTO learner_cosmetics (learner_id, cosmetic_item_id, is_equipped) VALUES
(3, 4, TRUE), (3, 1, FALSE);

-- Point transactions
INSERT INTO point_transactions (learner_id, source_type, source_id, points) VALUES
(3, 'PRACTICE_ATTEMPT', 1, 10),
(3, 'PRACTICE_ATTEMPT', 2, 15),
(3, 'QUIZ_ATTEMPT', 1, 20),
(3, 'MINIGAME_ATTEMPT', 1, 25),
(3, 'ACHIEVEMENT', 1, 50),
(3, 'ACHIEVEMENT', 2, 50),
(3, 'PRACTICE_ATTEMPT', 3, 10),
(3, 'STREAK_BONUS', NULL, 90);

INSERT INTO leaderboards (learner_id, total_points) VALUES (3, 270);

-- Learner daily challenges
INSERT INTO learner_daily_challenges (learner_id, challenge_id, completed, completed_at) VALUES
(3, 1, TRUE, '2026-06-10 08:25:00'),
(3, 2, TRUE, '2026-06-10 08:25:00');
