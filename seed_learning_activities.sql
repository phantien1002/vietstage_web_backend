-- Learning-activity seed for the CURRENT JPA schema.
--
-- Run this after the application has created/updated its schema. It is
-- idempotent and only adds content to lessons that do not already have the
-- matching order index. The app receives these rows through:
--   GET /api/lessons/{lessonId}/quizzes
--   GET /api/lessons/{lessonId}/minigames

-- QUIZZES -------------------------------------------------------------------
INSERT INTO quizzes (lesson_id, title, question_type, note, question, options, correct_answer, order_index, created_at)
SELECT lesson_id, 'Nhận biết nốt Đô', 'NOTE_RECOGNITION', 'C4',
       'Đây là nốt gì trên khuông nhạc?', '["Đô", "Rê", "Mi", "Fa"]', 'Đô', 0, NOW()
FROM lessons
WHERE order_index = 1
  AND NOT EXISTS (
      SELECT 1 FROM quizzes q
      WHERE q.lesson_id = lessons.lesson_id AND q.order_index = 0
  );

INSERT INTO quizzes (lesson_id, title, question_type, note, question, options, correct_answer, order_index, created_at)
SELECT lesson_id, 'Nhận biết nốt Rê', 'NOTE_RECOGNITION', 'D4',
       'Nốt nào đứng ngay sau nốt Đô?', '["Mi", "Rê", "Sol", "La"]', 'Rê', 1, NOW()
FROM lessons
WHERE order_index = 1
  AND NOT EXISTS (
      SELECT 1 FROM quizzes q
      WHERE q.lesson_id = lessons.lesson_id AND q.order_index = 1
  );

INSERT INTO quizzes (lesson_id, title, question_type, note, question, options, correct_answer, order_index, created_at)
SELECT lesson_id, 'Đọc nốt cơ bản', 'NOTE_RECOGNITION', 'E4',
       'Nốt Mi nằm ở vị trí nào trong dãy Đô, Rê, Mi?', '["Thứ nhất", "Thứ hai", "Thứ ba", "Thứ tư"]', 'Thứ ba', 0, NOW()
FROM lessons
WHERE order_index = 2
  AND NOT EXISTS (
      SELECT 1 FROM quizzes q
      WHERE q.lesson_id = lessons.lesson_id AND q.order_index = 0
  );

-- RHYTHM MINI GAMES ---------------------------------------------------------
INSERT INTO minigame_challenges (lesson_id, title, challenge_type, content_json, difficulty, max_score, order_index, created_at)
SELECT lesson_id, 'Giữ nhịp cơ bản', 'RHYTHM_MATCH',
       '{"tempoBpm":80,"beats":[0.5,1.0,1.5,2.0]}', 'BEGINNER', 300, 0, NOW()
FROM lessons
WHERE order_index = 1
  AND NOT EXISTS (
      SELECT 1 FROM minigame_challenges m
      WHERE m.lesson_id = lessons.lesson_id AND m.order_index = 0
  );

INSERT INTO minigame_challenges (lesson_id, title, challenge_type, content_json, difficulty, max_score, order_index, created_at)
SELECT lesson_id, 'Giữ nhịp nâng cao', 'RHYTHM_MATCH',
       '{"tempoBpm":96,"beats":[0.5,1.0,1.5,2.0,2.5,3.0]}', 'BEGINNER', 500, 0, NOW()
FROM lessons
WHERE order_index = 2
  AND NOT EXISTS (
      SELECT 1 FROM minigame_challenges m
      WHERE m.lesson_id = lessons.lesson_id AND m.order_index = 0
  );
