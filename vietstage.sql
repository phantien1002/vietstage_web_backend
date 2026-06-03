-- =========================================================
-- VIETSTAGE POSTGRESQL SCRIPT - FULL COMPLIANT (REDIS FOR TOKENS)
-- Added: notifications, mini_games, mini_game_results
-- =========================================================

DROP TABLE IF EXISTS notifications CASCADE;              -- Bổ sung DROP
DROP TABLE IF EXISTS mini_game_results CASCADE;          -- Bổ sung DROP
DROP TABLE IF EXISTS mini_games CASCADE;                 -- Bổ sung DROP
DROP TABLE IF EXISTS learner_daily_challenges CASCADE;
DROP TABLE IF EXISTS daily_challenges CASCADE;
DROP TABLE IF EXISTS learner_achievements CASCADE;
DROP TABLE IF EXISTS achievements CASCADE;
DROP TABLE IF EXISTS leaderboards CASCADE;
DROP TABLE IF EXISTS learner_progress CASCADE;
DROP TABLE IF EXISTS instructor_feedback CASCADE;
DROP TABLE IF EXISTS practice_attempts CASCADE;
DROP TABLE IF EXISTS exercises CASCADE;
DROP TABLE IF EXISTS audio_references CASCADE;
DROP TABLE IF EXISTS lesson_contents CASCADE;
DROP TABLE IF EXISTS lesson_techniques CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS techniques CASCADE;
DROP TABLE IF EXISTS instruments CASCADE;
DROP TABLE IF EXISTS learner_profiles CASCADE;
DROP TABLE IF EXISTS instructor_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS skill_levels CASCADE;

-- 1. Trình độ học viên
CREATE TABLE skill_levels (
    id BIGSERIAL PRIMARY KEY,
    level_name VARCHAR(50) UNIQUE NOT NULL
);

-- 2. Tài khoản người dùng core
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK(role IN ('ADMIN','INSTRUCTOR','LEARNER')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Hồ sơ Học viên (Streaks & Chỉ số Gamification bề nổi)
CREATE TABLE learner_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    skill_level_id BIGINT REFERENCES skill_levels(id) ON DELETE SET NULL,
    favorite_instrument VARCHAR(100),
    total_practice_minutes INT DEFAULT 0
);

-- 4. Hồ sơ Giảng viên
CREATE TABLE instructor_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    specialization VARCHAR(200),
    biography TEXT,
    years_experience INT DEFAULT 0
);

-- BỔ SUNG 1: Hệ thống thông báo (Nối trực tiếp về bảng users)
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Nhạc cụ & Kỹ thuật đặc thù
CREATE TABLE instruments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE techniques (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT REFERENCES instruments(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- 6. Bài học giáo trình
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT REFERENCES instruments(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    difficulty VARCHAR(20) NOT NULL CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL
);

-- Bảng trung gian giải quyết quan hệ Nhiều - Nhiều (M:N) giữa Lesson và Technique
CREATE TABLE lesson_techniques (
    lesson_id BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    technique_id BIGINT REFERENCES techniques(id) ON DELETE CASCADE,
    PRIMARY KEY (lesson_id, technique_id)
);

-- 7. Chi tiết nội dung bài học & Bài tập tương tác (Chuẩn 3NF)
CREATE TABLE lesson_contents (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    content_text TEXT NOT NULL
);

CREATE TABLE audio_references (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    audio_url TEXT NOT NULL
);

CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL
);

-- 8. Kết quả thực hiện bài tập & Đánh giá chuyên sâu từ Instructor
CREATE TABLE practice_attempts (
    id BIGSERIAL PRIMARY KEY,
    learner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    exercise_id BIGINT REFERENCES exercises(id) ON DELETE CASCADE,
    pitch_score NUMERIC(5,2),
    rhythm_score NUMERIC(5,2),
    total_score NUMERIC(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE instructor_feedback (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT REFERENCES practice_attempts(id) ON DELETE CASCADE,
    instructor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    comment TEXT NOT NULL
);

CREATE TABLE learner_progress (
    learner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    lesson_id BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    stars INT DEFAULT 0 CHECK(stars BETWEEN 0 AND 3),
    completed BOOLEAN DEFAULT FALSE,
    PRIMARY KEY(learner_id, lesson_id)
);

-- 9. Thành tựu (Achievements)
CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE learner_achievements (
    learner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    achievement_id BIGINT REFERENCES achievements(id) ON DELETE CASCADE,
    PRIMARY KEY(learner_id, achievement_id)
);

-- BỔ SUNG 2: Danh mục các Mini Games cốt lõi
CREATE TABLE mini_games (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    game_type VARCHAR(50),
    difficulty VARCHAR(50),
    max_score INT DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BỔ SUNG 3: Lịch sử và kết quả chơi Mini Game của Học viên (Nối về Users & Mini Games)
CREATE TABLE mini_game_results (
    id BIGSERIAL PRIMARY KEY,
    learner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mini_game_id BIGINT NOT NULL REFERENCES mini_games(id) ON DELETE CASCADE,
    score INT DEFAULT 0,
    stars_earned INT CHECK (stars_earned BETWEEN 0 AND 3),
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. Thử thách hằng ngày (Daily Challenges)
CREATE TABLE daily_challenges (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL
);

CREATE TABLE learner_daily_challenges (
    learner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    challenge_id BIGINT REFERENCES daily_challenges(id) ON DELETE CASCADE,
    PRIMARY KEY(learner_id, challenge_id)
);

-- 11. Bảng xếp hạng độc lập
CREATE TABLE leaderboards (
    learner_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    total_points INT DEFAULT 0
);

-- Tối ưu tốc độ truy vấn cho hệ thống
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_lessons_instrument ON lessons(instrument_id);
CREATE INDEX idx_exercises_lesson ON exercises(lesson_id);
CREATE INDEX idx_attempts_learner ON practice_attempts(learner_id);
CREATE INDEX idx_leaderboards_points ON leaderboards(total_points DESC);

-- =========================================================
-- DỮ LIỆU MẪU CHUẨN (SEED DATA PRODUCTION)
-- =========================================================
INSERT INTO skill_levels(level_name) VALUES ('BEGINNER'),('INTERMEDIATE'),('ADVANCED');

INSERT INTO users (email, password_hash, full_name, role, is_active) VALUES 
('admin@vietstage.com', '$2a$10$Ep6jP15pbui3U5SqytWFYOI/8Rzf76NbZwA91DZM51H2FL5FlQOsS', 'System Admin', 'ADMIN', true),
-- Password thực tế: Instruc@123
('instructor@vietstage.com', '$2a$10$QyeuzCZ6qKim/jEmk83fBeWx3R3Yfvs3zkbCCUSOgnvIDhIZi/f4S', 'Nguyen Van A', 'INSTRUCTOR', true),
-- Password thực tế: Learner@123
('learner@vietstage.com', '$2a$10$T5WLoeD2gI9WvBLpF4wMOOlJhrsdgaShmNa.su5yjf8MG0s32EK.S', 'Tran Thi B', 'LEARNER', true);

INSERT INTO instructor_profiles(user_id,specialization,biography,years_experience)
VALUES (2,'Dan Tranh','Traditional music instructor',5);

INSERT INTO learner_profiles(user_id,skill_level_id,favorite_instrument,total_practice_minutes)
VALUES (3,1,'Dan Tranh',120);

INSERT INTO leaderboards(learner_id, total_points) 
VALUES (3, 250);

INSERT INTO instruments(name,description) VALUES
('Dan Tranh','Vietnamese zither'),
('Dan Bau','Vietnamese monochord'),
('Sao Truc','Vietnamese bamboo flute');

INSERT INTO techniques(instrument_id,name,description) VALUES
(1,'Basic Picking','Beginner technique'),
(3,'Breath Control','Flute technique');

INSERT INTO lessons(instrument_id,title,difficulty,created_by) VALUES
(1,'Introduction to Dan Tranh','BEGINNER',2);

INSERT INTO lesson_contents(lesson_id,content_text) VALUES (1,'Basic introduction lesson');
INSERT INTO audio_references(lesson_id,audio_url) VALUES (1,'https://example.com/audio1.mp3');
INSERT INTO exercises(lesson_id,title) VALUES (1,'Practice Exercise 1');
INSERT INTO achievements(name,description) VALUES ('First Lesson','Complete first lesson');
INSERT INTO daily_challenges(title) VALUES ('Practice 10 Minutes');

-- Seed thêm dữ liệu mẫu cho Mini Games & Notifications để test luồng
INSERT INTO mini_games(name, game_type, difficulty, max_score) VALUES
('Note Guesser', 'NOTE_QUIZ', 'EASY', 100);

INSERT INTO notifications(user_id, title, message, notification_type) VALUES
(3, 'Welcome to VietStage!', 'Start your journey with traditional instruments today.', 'SYSTEM');
