BEGIN;
TRUNCATE TABLE exercises CASCADE;
TRUNCATE TABLE lesson_contents CASCADE;
TRUNCATE TABLE assets CASCADE;
TRUNCATE TABLE lessons CASCADE;

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (1, 'sao_truc_level1_1_video', 2, 1, 1, 'BÀI 1', 'Làm quen Sáo Trúc', 'APPROVED', true, 1);
        

            INSERT INTO assets (id, file_path, file_type, file_size, uploaded_by_user_id)
            VALUES (1, 'Cách cầm sáo trúc & lấy hơi.', 'VIDEO', 0, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index)
            VALUES (1, 1, 'VIDEO_CUE', 1, 1);
            

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (2, 'Node2', 2, 2, 1, 'BÀI 1', 'Nốt Si (B4)', 'APPROVED', true, 2);
        

            INSERT INTO assets (id, file_path, file_type, file_size, uploaded_by_user_id)
            VALUES (2, 'Hướng dẫn thổi nốt Si.', 'VIDEO', 0, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index)
            VALUES (2, 2, 'VIDEO_CUE', 2, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (3, 2, 'TEACHER_SPEECH', 'intro', 2);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (4, 2, 'TEACHER_SPEECH', 'mid', 3);
            

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (3, 'Node3', 2, 2, 1, 'BÀI 2', 'Nốt La (A4)', 'APPROVED', true, 3);
        

            INSERT INTO assets (id, file_path, file_type, file_size, uploaded_by_user_id)
            VALUES (3, 'Hướng dẫn thổi nốt La.', 'VIDEO', 0, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index)
            VALUES (5, 3, 'VIDEO_CUE', 3, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (6, 3, 'TEACHER_SPEECH', 'intro', 2);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (7, 3, 'TEACHER_SPEECH', 'mid', 3);
            

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (4, 'Node4', 2, 2, 1, 'BÀI 3', 'Nốt Sol (G4)', 'APPROVED', true, 4);
        

            INSERT INTO assets (id, file_path, file_type, file_size, uploaded_by_user_id)
            VALUES (4, 'Hướng dẫn thổi nốt Sol.', 'VIDEO', 0, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index)
            VALUES (8, 4, 'VIDEO_CUE', 4, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (9, 4, 'TEACHER_SPEECH', 'intro', 2);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (10, 4, 'TEACHER_SPEECH', 'mid', 3);
            

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (5, 'Node5', 2, 2, 1, 'BÀI 4', 'Nốt Fa (F4)', 'APPROVED', true, 5);
        

            INSERT INTO assets (id, file_path, file_type, file_size, uploaded_by_user_id)
            VALUES (5, 'Hướng dẫn thổi nốt Fa.', 'VIDEO', 0, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index)
            VALUES (11, 5, 'VIDEO_CUE', 5, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (12, 5, 'TEACHER_SPEECH', 'intro', 2);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (13, 5, 'TEACHER_SPEECH', 'mid', 3);
            

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (6, 'Node6', 2, 2, 1, 'BÀI 5', 'Nốt Mi (E4)', 'APPROVED', true, 6);
        

            INSERT INTO assets (id, file_path, file_type, file_size, uploaded_by_user_id)
            VALUES (6, 'Hướng dẫn thổi nốt Mi.', 'VIDEO', 0, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index)
            VALUES (14, 6, 'VIDEO_CUE', 6, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (15, 6, 'TEACHER_SPEECH', 'intro', 2);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (16, 6, 'TEACHER_SPEECH', 'mid', 3);
            

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (7, 'Node7', 2, 2, 1, 'BÀI 6', 'Nốt Rê (D4)', 'APPROVED', true, 7);
        

            INSERT INTO assets (id, file_path, file_type, file_size, uploaded_by_user_id)
            VALUES (7, 'Hướng dẫn thổi nốt Rê.', 'VIDEO', 0, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index)
            VALUES (17, 7, 'VIDEO_CUE', 7, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (18, 7, 'TEACHER_SPEECH', 'intro', 2);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (19, 7, 'TEACHER_SPEECH', 'mid', 3);
            

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (8, 'Node8', 2, 2, 1, 'BÀI 7', 'Nốt Đô (C4)', 'APPROVED', true, 8);
        

            INSERT INTO assets (id, file_path, file_type, file_size, uploaded_by_user_id)
            VALUES (8, 'Hướng dẫn thổi nốt Đô.', 'VIDEO', 0, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index)
            VALUES (20, 8, 'VIDEO_CUE', 8, 1);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (21, 8, 'TEACHER_SPEECH', 'intro', 2);
            

            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES (22, 8, 'TEACHER_SPEECH', 'mid', 3);
            

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (9, 'sao_truc_level3_1', 2, 3, 1, 'BÀI 1', 'Khúc Nhạc Vui (Khung 1)', 'APPROVED', true, 9);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (10, 'sao_truc_level3_2', 2, 3, 1, 'BÀI 2', 'Khúc Nhạc Vui (Khung 2)', 'APPROVED', true, 10);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (11, 'sao_truc_level3_3', 2, 3, 1, 'BÀI 3', 'Khúc Nhạc Vui (Khung 3)', 'APPROVED', true, 11);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (12, 'sao_truc_level3_4', 2, 3, 1, 'BÀI 4', 'Khúc Nhạc Vui (Khung 4)', 'APPROVED', true, 12);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (13, 'sao_truc_level3_5', 2, 3, 1, 'BÀI 5', 'Khúc Nhạc Vui (Khung 5)', 'APPROVED', true, 13);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (14, 'sao_truc_level3_6', 2, 3, 1, 'BÀI 6', 'Khúc Nhạc Vui (Hoàn chỉnh)', 'APPROVED', true, 14);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (15, 'sao_truc_level4_1', 2, 4, 1, 'BÀI 1', 'Inh Lả Ơi (Câu 1)', 'APPROVED', true, 15);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (16, 'sao_truc_level4_2', 2, 4, 1, 'BÀI 2', 'Inh Lả Ơi (Câu 2)', 'APPROVED', true, 16);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (17, 'sao_truc_level4_3', 2, 4, 1, 'BÀI 3', 'Inh Lả Ơi (Câu 3)', 'APPROVED', true, 17);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (18, 'sao_truc_level4_4', 2, 4, 1, 'BÀI 4', 'Inh Lả Ơi (Câu 4)', 'APPROVED', true, 18);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (19, 'sao_truc_level4_5', 2, 4, 1, 'BÀI 5', 'Inh Lả Ơi (Hoàn chỉnh)', 'APPROVED', true, 19);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (20, 'sao_truc_level5_1', 2, 5, 1, 'BÀI 1', 'Futari no Kimochi (Đoạn 1 - P1)', 'APPROVED', true, 20);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (21, 'sao_truc_level5_2', 2, 5, 1, 'BÀI 2', 'Futari no Kimochi (Đoạn 1 - P2)', 'APPROVED', true, 21);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (22, 'sao_truc_level5_3', 2, 5, 1, 'BÀI 3', 'Futari no Kimochi (Đoạn 1 - HC)', 'APPROVED', true, 22);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (23, 'sao_truc_level5_4', 2, 5, 1, 'BÀI 4', 'Futari no Kimochi (Đoạn 2 - P1)', 'APPROVED', true, 23);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (24, 'sao_truc_level5_5', 2, 5, 1, 'BÀI 5', 'Futari no Kimochi (Đoạn 2 - P2)', 'APPROVED', true, 24);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (25, 'sao_truc_level5_6', 2, 5, 1, 'BÀI 6', 'Futari no Kimochi (Đoạn 2 - HC)', 'APPROVED', true, 25);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (26, 'sao_truc_level5_7', 2, 5, 1, 'BÀI 7', 'Futari no Kimochi (Hoàn chỉnh toàn bài)', 'APPROVED', true, 26);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (27, 'Node35', 2, 6, 1, 'BÀI 1', 'Gặp Mẹ Trong Mơ (Khung 1)', 'APPROVED', true, 27);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (28, 'Node36', 2, 6, 1, 'BÀI 2', 'Gặp Mẹ Trong Mơ (Khung 2)', 'APPROVED', true, 28);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (29, 'Node37', 2, 6, 1, 'BÀI 3', 'Gặp Mẹ Trong Mơ (Khung 3)', 'APPROVED', true, 29);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (30, 'Node38', 2, 6, 1, 'BÀI 4', 'Gặp Mẹ Trong Mơ (Khung 4)', 'APPROVED', true, 30);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (31, 'Node39', 2, 6, 1, 'BÀI 5', 'Gặp Mẹ Trong Mơ (Khung 5)', 'APPROVED', true, 31);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (32, 'Node40', 2, 6, 1, 'BÀI 6', 'Gặp Mẹ Trong Mơ (Khung 6)', 'APPROVED', true, 32);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (33, 'Node41', 2, 6, 1, 'BÀI 7', 'Gặp Mẹ Trong Mơ (Khung 7)', 'APPROVED', true, 33);
        

        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES (34, 'Node42', 2, 6, 1, 'BÀI 8', 'Gặp Mẹ Trong Mơ (Hoàn chỉnh)', 'APPROVED', true, 34);
        
COMMIT;