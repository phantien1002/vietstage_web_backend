
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

CREATE TABLE skill_levels (
    id BIGSERIAL PRIMARY KEY,
    level_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK(role IN ('ADMIN','INSTRUCTOR','LEARNER')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE learner_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    skill_level_id BIGINT REFERENCES skill_levels(id),
    favorite_instrument VARCHAR(100),
    total_practice_minutes INT DEFAULT 0
);

CREATE TABLE instructor_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    specialization VARCHAR(200),
    biography TEXT,
    years_experience INT DEFAULT 0
);

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

CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT REFERENCES instruments(id),
    title VARCHAR(200) NOT NULL,
    difficulty VARCHAR(20),
    created_by BIGINT REFERENCES users(id)
);

CREATE TABLE lesson_techniques (
    lesson_id BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    technique_id BIGINT REFERENCES techniques(id) ON DELETE CASCADE,
    PRIMARY KEY (lesson_id, technique_id)
);

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

CREATE TABLE practice_attempts (
    id BIGSERIAL PRIMARY KEY,
    learner_id BIGINT REFERENCES users(id),
    exercise_id BIGINT REFERENCES exercises(id),
    pitch_score NUMERIC(5,2),
    rhythm_score NUMERIC(5,2),
    total_score NUMERIC(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE instructor_feedback (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT REFERENCES practice_attempts(id) ON DELETE CASCADE,
    instructor_id BIGINT REFERENCES users(id),
    comment TEXT NOT NULL
);

CREATE TABLE learner_progress (
    learner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    lesson_id BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    stars INT DEFAULT 0 CHECK(stars BETWEEN 0 AND 3),
    completed BOOLEAN DEFAULT FALSE,
    PRIMARY KEY(learner_id, lesson_id)
);

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

CREATE TABLE daily_challenges (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL
);

CREATE TABLE learner_daily_challenges (
    learner_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    challenge_id BIGINT REFERENCES daily_challenges(id) ON DELETE CASCADE,
    PRIMARY KEY(learner_id, challenge_id)
);

CREATE TABLE leaderboards (
    learner_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    total_points INT DEFAULT 0
);

-- PASSWORD THỰC TẾ CHO TOÀN BỘ ACCOUNT DEMO:
-- Password@123

INSERT INTO skill_levels(level_name)
VALUES ('BEGINNER'),('INTERMEDIATE'),('ADVANCED');

INSERT INTO users(email,password_hash,full_name,role)
VALUES
('admin@vietstage.com','$2a$10$Password123HashDemo','System Admin','ADMIN'),
('teacher1@vietstage.com','$2a$10$Password123HashDemo','Nguyen Van A','INSTRUCTOR'),
('learner1@vietstage.com','$2a$10$Password123HashDemo','Tran Van B','LEARNER');

INSERT INTO instructor_profiles(user_id,specialization,biography,years_experience)
VALUES (2,'Dan Tranh','Traditional music instructor',5);

INSERT INTO learner_profiles(user_id,skill_level_id,favorite_instrument,total_practice_minutes)
VALUES (3,1,'Dan Tranh',120);

INSERT INTO instruments(name,description)
VALUES
('Dan Tranh','Vietnamese zither'),
('Dan Bau','Vietnamese monochord'),
('Sao Truc','Vietnamese bamboo flute');

INSERT INTO techniques(instrument_id,name,description)
VALUES
(1,'Basic Picking','Beginner technique'),
(3,'Breath Control','Flute technique');

INSERT INTO lessons(instrument_id,title,difficulty,created_by)
VALUES
(1,'Introduction to Dan Tranh','BEGINNER',2);

INSERT INTO lesson_contents(lesson_id,content_text)
VALUES (1,'Basic introduction lesson');

INSERT INTO audio_references(lesson_id,audio_url)
VALUES (1,'https://example.com/audio1.mp3');

INSERT INTO exercises(lesson_id,title)
VALUES (1,'Practice Exercise 1');

INSERT INTO achievements(name,description)
VALUES ('First Lesson','Complete first lesson');

INSERT INTO daily_challenges(title)
VALUES ('Practice 10 Minutes');
