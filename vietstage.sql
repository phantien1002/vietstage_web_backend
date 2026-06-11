-- =========================================================
-- VIETSTAGE POSTGRESQL SCRIPT v2.0
-- Nâng cấp từ bản gốc — kết hợp góp ý review + phân tích proposal
-- Thay đổi chính:
--   [1]  skill_levels nối lessons (thay enum difficulty)
--   [2]  learner_profiles bổ sung streak & adaptive difficulty
--   [3]  lessons bổ sung status, order_index, description, updated_at
--   [4]  audio_references → lesson_assets (gộp + mở rộng type)
--   [5]  exercises bổ sung beat_map, pass_threshold
--   [6]  practice_attempts bổ sung 3 AI metrics + session_id
--   [7]  learner_progress bổ sung started_at, completed_at, sync_status
--   [8]  mini_game_results đổi played_at → started_at + completed_at
--   [9]  Bảng mới: practice_sessions, lesson_mini_games, learner_feedback,
--                  content_reviews, cosmetic_items, learner_cosmetics,
--                  point_transactions, app_configs
--   [10] updated_at cho toàn bộ bảng tương tác với user
--   [11] Seed data đầy đủ hơn cho tất cả bảng mới
-- =========================================================

-- =========================================================
-- DROP TABLES (theo thứ tự dependency)
-- =========================================================
DROP TABLE IF EXISTS app_configs CASCADE;
DROP TABLE IF EXISTS content_reviews CASCADE;
DROP TABLE IF EXISTS point_transactions CASCADE;
DROP TABLE IF EXISTS learner_cosmetics CASCADE;
DROP TABLE IF EXISTS cosmetic_items CASCADE;
DROP TABLE IF EXISTS learner_daily_challenges CASCADE;
DROP TABLE IF EXISTS daily_challenges CASCADE;
DROP TABLE IF EXISTS leaderboards CASCADE;
DROP TABLE IF EXISTS learner_achievements CASCADE;
DROP TABLE IF EXISTS achievements CASCADE;
DROP TABLE IF EXISTS mini_game_results CASCADE;
DROP TABLE IF EXISTS lesson_mini_games CASCADE;          -- [MỚI]
DROP TABLE IF EXISTS mini_games CASCADE;
DROP TABLE IF EXISTS learner_progress CASCADE;
DROP TABLE IF EXISTS learner_feedback CASCADE;           -- [MỚI]
DROP TABLE IF EXISTS instructor_feedback CASCADE;
DROP TABLE IF EXISTS practice_attempts CASCADE;
DROP TABLE IF EXISTS practice_sessions CASCADE;          -- [MỚI]
DROP TABLE IF EXISTS exercises CASCADE;
DROP TABLE IF EXISTS lesson_assets CASCADE;              -- [MỚI] thay audio_references
DROP TABLE IF EXISTS audio_references CASCADE;           -- giữ lại DROP phòng migration cũ
DROP TABLE IF EXISTS lesson_contents CASCADE;
DROP TABLE IF EXISTS lesson_techniques CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS techniques CASCADE;
DROP TABLE IF EXISTS instruments CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS learner_profiles CASCADE;
DROP TABLE IF EXISTS instructor_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS skill_levels CASCADE;


-- =========================================================
-- 1. Trình độ học viên
-- =========================================================
CREATE TABLE skill_levels (
                              id         BIGSERIAL PRIMARY KEY,
                              level_name VARCHAR(50) UNIQUE NOT NULL  -- BEGINNER | INTERMEDIATE | ADVANCED
);


-- =========================================================
-- 2. Tài khoản người dùng core
-- =========================================================
CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       email         VARCHAR(150) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name     VARCHAR(150) NOT NULL,
                       role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','INSTRUCTOR','LEARNER')),
                       is_active     BOOLEAN   DEFAULT TRUE,
                       created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- [10]
);


-- =========================================================
-- 3. Hồ sơ Học viên
--    [2] Bổ sung: streak system + adaptive difficulty
-- =========================================================
CREATE TABLE learner_profiles (
                                  user_id                BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                  skill_level_id         BIGINT REFERENCES skill_levels(id) ON DELETE SET NULL,
                                  favorite_instrument    VARCHAR(100),
                                  total_practice_minutes INT     DEFAULT 0,

    -- Streak system (proposal: daily challenge streak rewards)
                                  current_streak         INT     DEFAULT 0,
                                  longest_streak         INT     DEFAULT 0,
                                  last_practice_date     DATE,

    -- Adaptive difficulty (proposal: rolling accuracy last 10 attempts)
                                  adaptive_difficulty    VARCHAR(20) DEFAULT 'BEGINNER'
                                      CHECK (adaptive_difficulty IN ('BEGINNER','INTERMEDIATE','ADVANCED')),

                                  updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- [10]
);


-- =========================================================
-- 4. Hồ sơ Giảng viên
-- =========================================================
CREATE TABLE instructor_profiles (
                                     user_id          BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                     specialization   VARCHAR(200),
                                     biography        TEXT,
                                     years_experience INT     DEFAULT 0,
                                     updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- [10]
);


-- =========================================================
-- 5. Hệ thống thông báo
-- =========================================================
CREATE TABLE notifications (
                               id                BIGSERIAL PRIMARY KEY,
                               user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               title             VARCHAR(200) NOT NULL,
                               message           TEXT NOT NULL,
    -- SYSTEM | ACHIEVEMENT | STREAK | FEEDBACK | CHALLENGE
                               notification_type VARCHAR(50),
                               is_read           BOOLEAN   DEFAULT FALSE,
                               created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- [10]
);


-- =========================================================
-- 6. Nhạc cụ & Kỹ thuật đặc thù
-- =========================================================
CREATE TABLE instruments (
                             id          BIGSERIAL PRIMARY KEY,
                             name        VARCHAR(100) UNIQUE NOT NULL,
                             description TEXT,
                             icon_url    TEXT  -- Asset icon hiển thị trong game Godot
);

CREATE TABLE techniques (
                            id            BIGSERIAL PRIMARY KEY,
                            instrument_id BIGINT REFERENCES instruments(id) ON DELETE CASCADE,
                            name          VARCHAR(100) NOT NULL,
                            description   TEXT,
                            guide_url     TEXT  -- Link video/ảnh minh hoạ kỹ thuật (proposal: finger placement)
);


-- =========================================================
-- 7. Bài học giáo trình
--    [1] difficulty enum → FK skill_level_id
--    [3] Bổ sung: status (approval workflow), order_index, description, updated_at
-- =========================================================
CREATE TABLE lessons (
                         id             BIGSERIAL PRIMARY KEY,
                         instrument_id  BIGINT REFERENCES instruments(id) ON DELETE CASCADE,
                         skill_level_id BIGINT REFERENCES skill_levels(id) ON DELETE SET NULL,  -- [1]
                         title          VARCHAR(200) NOT NULL,
                         description    TEXT,                                                    -- [3]
    -- Content approval workflow (proposal: admin review & approve)
                         status         VARCHAR(20) DEFAULT 'DRAFT'
                             CHECK (status IN ('DRAFT','PENDING','APPROVED','REJECTED')),  -- [3]
                         order_index    INT     DEFAULT 0,   -- Thứ tự trong curriculum              [3]
                         created_by     BIGINT REFERENCES users(id) ON DELETE SET NULL,
                         created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- [10]
);

-- Bảng trung gian N-N giữa Lesson và Technique
CREATE TABLE lesson_techniques (
                                   lesson_id    BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
                                   technique_id BIGINT REFERENCES techniques(id) ON DELETE CASCADE,
                                   PRIMARY KEY (lesson_id, technique_id)
);


-- =========================================================
-- 8. Chi tiết nội dung bài học
-- =========================================================
CREATE TABLE lesson_contents (
                                 id           BIGSERIAL PRIMARY KEY,
                                 lesson_id    BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
                                 content_text TEXT NOT NULL,
                                 order_index  INT DEFAULT 0
);

-- [4] lesson_assets thay thế audio_references — gộp tất cả media vào 1 bảng
--     Hỗ trợ: REFERENCE_AUDIO, SHEET_IMAGE, TECHNIQUE_VIDEO, BEAT_MAP
--     (proposal: instructor upload sheet notation, technique video, reference audio)
CREATE TABLE lesson_assets (
                               id           BIGSERIAL PRIMARY KEY,
                               lesson_id    BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
                               asset_type   VARCHAR(30) NOT NULL
                                   CHECK (asset_type IN ('REFERENCE_AUDIO','SHEET_IMAGE','TECHNIQUE_VIDEO','BEAT_MAP')),
                               asset_url    TEXT NOT NULL,          -- S3 / GCP Cloud Storage URL
                               tempo_bpm    INT,                    -- Dành cho REFERENCE_AUDIO & BEAT_MAP
                               duration_sec NUMERIC(7,2),          -- Độ dài file audio (giây)
                               created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- 9. Bài tập thực hành
--    [5] Bổ sung: beat_map_asset_id, pass_threshold, order_index
-- =========================================================
CREATE TABLE exercises (
                           id                BIGSERIAL PRIMARY KEY,
                           lesson_id         BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
                           title             VARCHAR(200) NOT NULL,
                           description       TEXT,
    -- Liên kết beat map cho rhythm evaluation
    -- (proposal: onset timing vs reference beat map)
                           beat_map_asset_id BIGINT REFERENCES lesson_assets(id) ON DELETE SET NULL,  -- [5]
    -- Ngưỡng điểm tối thiểu để pass
    -- (proposal: instructor sets scoring thresholds)
                           pass_threshold    NUMERIC(5,2) DEFAULT 60.00,  -- [5]
                           order_index       INT DEFAULT 0
);


-- =========================================================
-- 10. Session luyện tập
--     [MỚI] Track phiên học, phục vụ analytics & offline sync
--     (proposal: session duration, retention metrics, offline mode)
-- =========================================================
CREATE TABLE practice_sessions (
                                   id          BIGSERIAL PRIMARY KEY,
                                   learner_id  BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                   started_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   ended_at    TIMESTAMP,
    -- Offline sync (proposal: cached lessons, sync when reconnected)
                                   sync_status VARCHAR(20) DEFAULT 'SYNCED'
                                       CHECK (sync_status IN ('SYNCED','PENDING_SYNC','CONFLICT'))
);


-- =========================================================
-- 11. Kết quả thực hiện bài tập
--     [6] Bổ sung 3 AI metrics mới + session_id + sync_status
--     (proposal: 5 scoring dimensions từ GDExtension C++)
-- =========================================================
CREATE TABLE practice_attempts (
                                   id                  BIGSERIAL PRIMARY KEY,
                                   session_id          BIGINT REFERENCES practice_sessions(id) ON DELETE SET NULL,  -- [6]
                                   learner_id          BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                   exercise_id         BIGINT REFERENCES exercises(id) ON DELETE CASCADE,

    -- AI scoring (proposal: pitch, rhythm, dynamics, tonal quality, breath pattern)
                                   pitch_score         NUMERIC(5,2),
                                   rhythm_score        NUMERIC(5,2),
                                   dynamics_score      NUMERIC(5,2),        -- [6] Proposal: dynamics metric
                                   tonal_quality_score NUMERIC(5,2),        -- [6] Proposal: spectral centroid (string)
                                   breath_score        NUMERIC(5,2),        -- [6] Proposal: breath pattern (sáo trúc)
                                   total_score         NUMERIC(5,2),        -- Composite từ scoring engine

    -- Offline sync
                                   sync_status         VARCHAR(20) DEFAULT 'SYNCED'
                                       CHECK (sync_status IN ('SYNCED','PENDING_SYNC','CONFLICT')),
                                   created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Đánh giá chuyên sâu từ Instructor
CREATE TABLE instructor_feedback (
                                     id            BIGSERIAL PRIMARY KEY,
                                     attempt_id    BIGINT REFERENCES practice_attempts(id) ON DELETE CASCADE,
                                     instructor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
                                     comment       TEXT NOT NULL,
                                     created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- [MỚI] Feedback của Learner cho Bài học
CREATE TABLE learner_feedback (
                                  id         BIGSERIAL PRIMARY KEY,
                                  learner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                  lesson_id  BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
                                  rating     INT CHECK (rating BETWEEN 1 AND 5),
                                  comment    TEXT,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  UNIQUE (learner_id, lesson_id)  -- Mỗi learner chỉ feedback 1 lần/bài
);


-- =========================================================
-- 12. Tiến độ học viên
--     [7] Bổ sung: started_at, completed_at, sync_status, updated_at
-- =========================================================
CREATE TABLE learner_progress (
                                  learner_id   BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                  lesson_id    BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
                                  stars        INT     DEFAULT 0 CHECK (stars BETWEEN 0 AND 3),
                                  completed    BOOLEAN DEFAULT FALSE,
                                  started_at   TIMESTAMP,                                  -- [7]
                                  completed_at TIMESTAMP,                                  -- [7]
                                  sync_status  VARCHAR(20) DEFAULT 'SYNCED'
                                      CHECK (sync_status IN ('SYNCED','PENDING_SYNC','CONFLICT')),  -- [7]
                                  updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,        -- [10]
                                  PRIMARY KEY (learner_id, lesson_id)
);

-- [MỚI] Content approval workflow
-- (proposal: admin review & approve lessons trước khi publish)
CREATE TABLE content_reviews (
                                 id          BIGSERIAL PRIMARY KEY,
                                 lesson_id   BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
                                 reviewer_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
                                 status      VARCHAR(20) NOT NULL CHECK (status IN ('APPROVED','REJECTED')),
                                 comment     TEXT,
                                 reviewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =========================================================
-- 13. Thành tựu (Achievements)
-- =========================================================
CREATE TABLE achievements (
                              id             BIGSERIAL PRIMARY KEY,
                              name           VARCHAR(100) NOT NULL,
                              description    TEXT,
                              icon_url       TEXT,
    -- Điều kiện unlock dạng JSON để linh hoạt mở rộng sau
    -- Ví dụ: {"type":"STREAK_DAYS","threshold":7}
                              condition_json TEXT
);

CREATE TABLE learner_achievements (
                                      learner_id     BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                      achievement_id BIGINT REFERENCES achievements(id) ON DELETE CASCADE,
                                      earned_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- [10]
                                      PRIMARY KEY (learner_id, achievement_id)
);


-- =========================================================
-- 14. Cosmetic Items — Virtual Room Customization
--     [MỚI] (proposal: unlock cosmetic rewards for virtual room)
-- =========================================================
CREATE TABLE cosmetic_items (
                                id           BIGSERIAL PRIMARY KEY,
                                name         VARCHAR(100) NOT NULL,
    -- ROOM_DECOR | AVATAR_SKIN | INSTRUMENT_SKIN
                                item_type    VARCHAR(50)  NOT NULL,
                                asset_url    TEXT,
    -- Điều kiện mở khoá: ACHIEVEMENT | STARS | POINTS | DEFAULT
                                unlock_type  VARCHAR(30) CHECK (unlock_type IN ('ACHIEVEMENT','STARS','POINTS','DEFAULT')),
                                unlock_value INT DEFAULT 0  -- Ngưỡng số sao/điểm cần đạt
);

CREATE TABLE learner_cosmetics (
                                   learner_id       BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                   cosmetic_item_id BIGINT REFERENCES cosmetic_items(id) ON DELETE CASCADE,
                                   unlocked_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   is_equipped      BOOLEAN DEFAULT FALSE,  -- Đang trang bị hay chỉ sở hữu
                                   PRIMARY KEY (learner_id, cosmetic_item_id)
);


-- =========================================================
-- 15. Mini Games
--     [MỚI] lesson_mini_games: bảng trung gian N-N Lesson ↔ Mini Game
--     [8]   mini_game_results: played_at → started_at + completed_at
-- =========================================================
CREATE TABLE mini_games (
                            id         BIGSERIAL PRIMARY KEY,
                            name       VARCHAR(120) NOT NULL,
    -- RHYTHM_MATCH | NOTE_QUIZ | MELODY_COMPLETE
                            game_type  VARCHAR(50),
                            difficulty VARCHAR(20) CHECK (difficulty IN ('EASY','MEDIUM','HARD')),
                            max_score  INT     DEFAULT 100,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- [MỚI] Bảng trung gian N-N Lesson ↔ Mini Game
CREATE TABLE lesson_mini_games (
                                   lesson_id    BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
                                   mini_game_id BIGINT REFERENCES mini_games(id) ON DELETE CASCADE,
                                   PRIMARY KEY (lesson_id, mini_game_id)
);

-- [8] Bổ sung started_at + completed_at thay cho played_at
CREATE TABLE mini_game_results (
                                   id           BIGSERIAL PRIMARY KEY,
                                   learner_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   mini_game_id BIGINT NOT NULL REFERENCES mini_games(id) ON DELETE CASCADE,
                                   score        INT DEFAULT 0,
                                   stars_earned INT CHECK (stars_earned BETWEEN 0 AND 3),
                                   started_at   TIMESTAMP,                              -- [8]
                                   completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   -- [8]
                                   sync_status  VARCHAR(20) DEFAULT 'SYNCED'
                                       CHECK (sync_status IN ('SYNCED','PENDING_SYNC','CONFLICT')),
                                   updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP    -- [10]
);


-- =========================================================
-- 16. Hệ thống điểm số & Xếp hạng
--     [MỚI] point_transactions: audit log mọi nguồn điểm
-- =========================================================

-- Log nguồn điểm — tránh corruption leaderboard, hỗ trợ audit
-- source_type: PRACTICE | MINI_GAME | ACHIEVEMENT | DAILY_CHALLENGE | STREAK_BONUS
CREATE TABLE point_transactions (
                                    id          BIGSERIAL PRIMARY KEY,
                                    learner_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                    source_type VARCHAR(30) NOT NULL,
                                    source_id   BIGINT,   -- FK logic tới bảng tương ứng (không enforce FK cứng vì đa nguồn)
                                    points      INT NOT NULL,
                                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng xếp hạng — aggregate từ point_transactions
CREATE TABLE leaderboards (
                              learner_id   BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                              total_points INT     DEFAULT 0,
                              updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- [10]
);


-- =========================================================
-- 17. Thử thách hằng ngày (Daily Challenges)
-- =========================================================
CREATE TABLE daily_challenges (
                                  id             BIGSERIAL PRIMARY KEY,
                                  title          VARCHAR(200) NOT NULL,
                                  description    TEXT,
                                  instrument_id  BIGINT REFERENCES instruments(id) ON DELETE SET NULL,
                                  reward_points  INT  DEFAULT 10,
                                  challenge_date DATE DEFAULT CURRENT_DATE
);

CREATE TABLE learner_daily_challenges (
                                          learner_id   BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                          challenge_id BIGINT REFERENCES daily_challenges(id) ON DELETE CASCADE,
                                          completed    BOOLEAN   DEFAULT FALSE,
                                          completed_at TIMESTAMP,
                                          PRIMARY KEY (learner_id, challenge_id)
);


-- =========================================================
-- 18. Cấu hình hệ thống
--     [MỚI] Feature toggles & scoring parameters
--     (proposal: admin configures scoring weights, difficulty curves)
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
-- Core
CREATE INDEX idx_users_email             ON users(email);
CREATE INDEX idx_users_role              ON users(role);
-- Lessons
CREATE INDEX idx_lessons_instrument      ON lessons(instrument_id);
CREATE INDEX idx_lessons_skill_level     ON lessons(skill_level_id);
CREATE INDEX idx_lessons_status          ON lessons(status);
-- Exercises & Assets
CREATE INDEX idx_exercises_lesson        ON exercises(lesson_id);
CREATE INDEX idx_lesson_assets_lesson    ON lesson_assets(lesson_id);
CREATE INDEX idx_lesson_assets_type      ON lesson_assets(asset_type);
-- Practice
CREATE INDEX idx_sessions_learner        ON practice_sessions(learner_id);
CREATE INDEX idx_attempts_learner        ON practice_attempts(learner_id);
CREATE INDEX idx_attempts_exercise       ON practice_attempts(exercise_id);
CREATE INDEX idx_attempts_session        ON practice_attempts(session_id);
-- Offline sync (partial index — chỉ index record chưa sync)
CREATE INDEX idx_progress_sync           ON learner_progress(sync_status)
    WHERE sync_status != 'SYNCED';
CREATE INDEX idx_attempts_sync           ON practice_attempts(sync_status)
    WHERE sync_status != 'SYNCED';
-- Progress & Leaderboard
CREATE INDEX idx_progress_learner        ON learner_progress(learner_id);
CREATE INDEX idx_progress_lesson         ON learner_progress(lesson_id);
CREATE INDEX idx_leaderboards_points     ON leaderboards(total_points DESC);
CREATE INDEX idx_point_tx_learner        ON point_transactions(learner_id);
-- Notifications
CREATE INDEX idx_notifications_user      ON notifications(user_id);
CREATE INDEX idx_notifications_unread    ON notifications(user_id)
    WHERE is_read = FALSE;
-- Mini games & Daily challenges
CREATE INDEX idx_mini_results_learner    ON mini_game_results(learner_id);
CREATE INDEX idx_daily_challenge_date    ON daily_challenges(challenge_date);


-- =========================================================
-- SEED DATA
-- =========================================================

INSERT INTO skill_levels (level_name) VALUES
                                          ('BEGINNER'), ('INTERMEDIATE'), ('ADVANCED');

-- Password thực tế: Admin@123 / Instruc@123 / Learner@123
INSERT INTO users (email, password_hash, full_name, role, is_active) VALUES
                                                                         ('admin@vietstage.com',
                                                                          '$2a$10$Ep6jP15pbui3U5SqytWFYOI/8Rzf76NbZwA91DZM51H2FL5FlQOsS',
                                                                          'System Admin', 'ADMIN', TRUE),
                                                                         ('instructor@vietstage.com',
                                                                          '$2a$10$QyeuzCZ6qKim/jEmk83fBeWx3R3Yfvs3zkbCCUSOgnvIDhIZi/f4S',
                                                                          'Nguyen Van A', 'INSTRUCTOR', TRUE),
                                                                         ('learner@vietstage.com',
                                                                          '$2a$10$T5WLoeD2gI9WvBLpF4wMOOlJhrsdgaShmNa.su5yjf8MG0s32EK.S',
                                                                          'Tran Thi B', 'LEARNER', TRUE);

INSERT INTO instructor_profiles (user_id, specialization, biography, years_experience)
VALUES (2, 'Dan Tranh', 'Traditional music instructor specializing in Vietnamese zither', 5);

INSERT INTO learner_profiles (user_id, skill_level_id, favorite_instrument, total_practice_minutes)
VALUES (3, 1, 'Dan Tranh', 120);

INSERT INTO leaderboards (learner_id, total_points)
VALUES (3, 250);

INSERT INTO instruments (name, description) VALUES
                                                ('Dan Tranh', 'Vietnamese 16-string zither'),
                                                ('Dan Bau',   'Vietnamese monochord'),
                                                ('Sao Truc',  'Vietnamese bamboo flute'),
                                                ('Trong',     'Vietnamese drum');

INSERT INTO techniques (instrument_id, name, description) VALUES
                                                              (1, 'Basic Picking',  'Beginner plucking technique for Dan Tranh'),
                                                              (1, 'Vibrato',        'String vibrato for expressive tone'),
                                                              (3, 'Breath Control', 'Sustain and attack control for Sao Truc'),
                                                              (3, 'Tone Shaping',   'Embouchure adjustment for tonal variation');

INSERT INTO lessons (instrument_id, skill_level_id, title, description, status, order_index, created_by) VALUES
                                                                                                             (1, 1, 'Introduction to Dan Tranh', 'Basic posture and string layout of the Dan Tranh', 'APPROVED', 1, 2),
                                                                                                             (1, 1, 'First Picking Patterns',    'Practice the foundational picking sequence',         'APPROVED', 2, 2),
                                                                                                             (3, 1, 'Introduction to Sao Truc',  'Breath control fundamentals for the bamboo flute',   'APPROVED', 1, 2);

INSERT INTO lesson_techniques (lesson_id, technique_id) VALUES
                                                            (1, 1), (2, 1), (2, 2), (3, 3);

INSERT INTO lesson_contents (lesson_id, content_text, order_index) VALUES
                                                                       (1, 'The Dan Tranh is a 16-string zither placed horizontally. Sit upright with the instrument in front of you.', 1),
                                                                       (1, 'Identify the strings by their pitch from low (left) to high (right).', 2),
                                                                       (3, 'Hold the Sao Truc horizontally with the mouth hole positioned at the lower lip.', 1);

INSERT INTO lesson_assets (lesson_id, asset_type, asset_url, tempo_bpm, duration_sec) VALUES
                                                                                          (1, 'REFERENCE_AUDIO', 'https://assets.vietstage.com/audio/dan-tranh-intro.mp3',      60, 45.0),
                                                                                          (1, 'SHEET_IMAGE',     'https://assets.vietstage.com/sheets/dan-tranh-intro.png',    NULL, NULL),
                                                                                          (2, 'REFERENCE_AUDIO', 'https://assets.vietstage.com/audio/picking-pattern-1.mp3',    72, 30.0),
                                                                                          (2, 'BEAT_MAP',        'https://assets.vietstage.com/beatmaps/picking-pattern-1.json', 72, 30.0),
                                                                                          (3, 'REFERENCE_AUDIO', 'https://assets.vietstage.com/audio/sao-truc-intro.mp3',       50, 60.0);

INSERT INTO exercises (lesson_id, title, description, pass_threshold, order_index) VALUES
                                                                                       (1, 'String Identification Quiz', 'Identify each string by its pitch',            60.00, 1),
                                                                                       (2, 'Picking Pattern Practice',   'Repeat the demonstrated picking sequence',      70.00, 1),
                                                                                       (3, 'Long Tone Exercise',         'Hold a single note for 4 beats with even tone', 65.00, 1);

INSERT INTO achievements (name, description, condition_json) VALUES
                                                                 ('First Lesson',  'Complete your very first exercise',         '{"type":"ATTEMPTS_COMPLETED","threshold":1}'),
                                                                 ('Hat-trick',     'Earn 3 stars on any lesson',                '{"type":"STARS_EARNED","threshold":3}'),
                                                                 ('Week Warrior',  'Maintain a 7-day practice streak',          '{"type":"STREAK_DAYS","threshold":7}'),
                                                                 ('Explorer',      'Try all 4 instruments at least once',       '{"type":"INSTRUMENTS_TRIED","threshold":4}');

INSERT INTO cosmetic_items (name, item_type, unlock_type, unlock_value) VALUES
                                                                            ('Bamboo Wallpaper', 'ROOM_DECOR',      'STARS',       10),
                                                                            ('Red Lantern Set',  'ROOM_DECOR',      'ACHIEVEMENT',  0),
                                                                            ('Golden Dan Tranh', 'INSTRUMENT_SKIN', 'POINTS',     500),
                                                                            ('Default Avatar',   'AVATAR_SKIN',     'DEFAULT',      0);

INSERT INTO mini_games (name, game_type, difficulty, max_score) VALUES
                                                                    ('Note Guesser',   'NOTE_QUIZ',       'EASY',   100),
                                                                    ('Rhythm Tap',     'RHYTHM_MATCH',    'EASY',   100),
                                                                    ('Melody Builder', 'MELODY_COMPLETE', 'MEDIUM', 150);

INSERT INTO lesson_mini_games (lesson_id, mini_game_id) VALUES
                                                            (1, 1), (2, 2), (3, 2);

INSERT INTO daily_challenges (title, description, instrument_id, reward_points, challenge_date) VALUES
                                                                                                    ('Practice 10 Minutes on Dan Tranh', 'Complete any Dan Tranh exercise for at least 10 minutes', 1, 15, CURRENT_DATE),
                                                                                                    ('3-Star Run',                       'Earn 3 stars on any lesson today',                        NULL, 20, CURRENT_DATE);

-- Config scoring weights (proposal: admin configures scoring parameters)
INSERT INTO app_configs (config_key, config_value, description, updated_by) VALUES
                                                                                ('scoring.pitch_weight',        '0.35',  'Weight of pitch score in composite total',         1),
                                                                                ('scoring.rhythm_weight',       '0.25',  'Weight of rhythm score in composite total',        1),
                                                                                ('scoring.dynamics_weight',     '0.15',  'Weight of dynamics score in composite total',      1),
                                                                                ('scoring.tonal_weight',        '0.15',  'Weight of tonal quality score (string instruments)',1),
                                                                                ('scoring.breath_weight',       '0.10',  'Weight of breath score (wind instruments only)',   1),
                                                                                ('feature.mini_games_enabled',  'true',  'Toggle mini-game feature globally',                1),
                                                                                ('feature.adaptive_difficulty', 'true',  'Enable adaptive difficulty adjustment',            1),
                                                                                ('difficulty.adaptive_window',  '10',    'Number of recent attempts for adaptive difficulty',1),
                                                                                ('offline.max_cached_lessons',  '20',    'Maximum lessons cached for offline play',          1);

INSERT INTO notifications (user_id, title, message, notification_type) VALUES
    (3, 'Welcome to VietStage!',
     'Start your journey with Vietnamese traditional instruments. Your first lesson is ready!',
     'SYSTEM');
