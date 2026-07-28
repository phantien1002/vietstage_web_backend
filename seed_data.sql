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
((SELECT user_id FROM users WHERE email = 'dantranh.master@vietstage.com'), (SELECT instrument_id FROM instruments WHERE name = 'Dan Tranh')),
((SELECT user_id FROM users WHERE email = 'saotruc.guru@vietstage.com'), (SELECT instrument_id FROM instruments WHERE name = 'Sao Truc')),
((SELECT user_id FROM users WHERE email = 'danbau.pro@vietstage.com'), (SELECT instrument_id FROM instruments WHERE name = 'Dan Bau')),
((SELECT user_id FROM users WHERE email = 'danbau.pro@vietstage.com'), (SELECT instrument_id FROM instruments WHERE name = 'Dan Tranh'))
ON CONFLICT DO NOTHING;

-- 4. LEARNER PROFILES
INSERT INTO learner_profiles (user_id, total_practice_seconds, current_streak, longest_streak) VALUES 
((SELECT user_id FROM users WHERE email = 'learner1@gmail.com'), 3600, 2, 5),
((SELECT user_id FROM users WHERE email = 'learner2@gmail.com'), 7200, 4, 10)
ON CONFLICT (user_id) DO NOTHING;

-- 5. LESSONS
INSERT INTO lessons (instrument_id, skill_level_id, created_by_user_id, title, description, status, order_index) VALUES 
((SELECT instrument_id FROM instruments WHERE name = 'Dan Tranh'), (SELECT skill_level_id FROM skill_levels WHERE level_code = 'BEGINNER'), (SELECT user_id FROM users WHERE email = 'dantranh.master@vietstage.com'), 'Bài 1: Làm quen Đàn Tranh', 'Cách gảy cơ bản', 'APPROVED', 1),
((SELECT instrument_id FROM instruments WHERE name = 'Dan Tranh'), (SELECT skill_level_id FROM skill_levels WHERE level_code = 'BEGINNER'), (SELECT user_id FROM users WHERE email = 'dantranh.master@vietstage.com'), 'Bài 2: Nốt nhạc cơ bản', 'Các nốt trên Đàn Tranh', 'APPROVED', 2),
((SELECT instrument_id FROM instruments WHERE name = 'Sao Truc'), (SELECT skill_level_id FROM skill_levels WHERE level_code = 'BEGINNER'), (SELECT user_id FROM users WHERE email = 'saotruc.guru@vietstage.com'), 'Bài 1: Cách thổi Sáo', 'Cách lấy hơi và thổi', 'APPROVED', 1)
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

-- 7. TECHNIQUES
INSERT INTO techniques (instrument_id, name, description, difficulty, guide_url) VALUES
((SELECT instrument_id FROM instruments WHERE name = 'Dan Tranh'), 'Rung', 'Ky thuat rung day co ban', 'EASY', 'http://example.com/rung'),
((SELECT instrument_id FROM instruments WHERE name = 'Dan Tranh'), 'Mo', 'Ky thuat mo day', 'MEDIUM', 'http://example.com/mo'),
((SELECT instrument_id FROM instruments WHERE name = 'Sao Truc'), 'Vuot', 'Ky thuat vuot nhip', 'EASY', 'http://example.com/vuot')
ON CONFLICT DO NOTHING;

