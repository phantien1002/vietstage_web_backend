import psycopg2
import sys
import uuid

def main():
    sys.stdout.reconfigure(encoding='utf-8')
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    
    emails = ['phuclong2710@gmail.com', 'thanhdattb19@gmail.com']
    
    try:
        conn = psycopg2.connect(url)
        cursor = conn.cursor()
        
        for email in emails:
            # 1. Get user_id
            cursor.execute("SELECT user_id FROM users WHERE email = %s", (email,))
            row = cursor.fetchone()
            if not row:
                print(f"User {email} not found, skipping...")
                continue
            uid = row[0]
            print(f"Processing user {email} (id={uid})")
            
            # 2. Add to learner_instruments
            for inst_id in [1, 2]:
                cursor.execute("SELECT id FROM learner_instruments WHERE learner_user_id = %s AND instrument_id = %s", (uid, inst_id))
                if not cursor.fetchone():
                    cursor.execute("""
                        INSERT INTO learner_instruments (learner_user_id, instrument_id, current_skill_level_id, adaptive_skill_level_id, selected_at)
                        VALUES (%s, %s, 1, 1, CURRENT_TIMESTAMP)
                    """, (uid, inst_id))
            
            # 3. Get all approved lessons for instruments 1 and 2
            cursor.execute("SELECT lesson_id FROM lessons WHERE instrument_id IN (1, 2) AND status = 'APPROVED'")
            lessons = cursor.fetchall()
            
            # 4. Upsert learner_lesson_progress
            for (lid,) in lessons:
                cursor.execute("SELECT id FROM learner_lesson_progress WHERE learner_user_id = %s AND lesson_id = %s", (uid, lid))
                if cursor.fetchone():
                    cursor.execute("""
                        UPDATE learner_lesson_progress 
                        SET status = 'COMPLETED', stars = 3, best_score = 100, unlocked_at = CURRENT_TIMESTAMP, 
                            started_at = CURRENT_TIMESTAMP, completed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                        WHERE learner_user_id = %s AND lesson_id = %s
                    """, (uid, lid))
                else:
                    cursor.execute("""
                        INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score, unlocked_at, started_at, completed_at, updated_at)
                        VALUES (%s, %s, 'COMPLETED', 3, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """, (uid, lid))
                    
            # 5. Update total_stars_earned in learner_profiles
            cursor.execute("SELECT SUM(stars) FROM learner_lesson_progress WHERE learner_user_id = %s", (uid,))
            total_stars = cursor.fetchone()[0] or 0
            
            # DO NOT update spendable_stars
            cursor.execute("""
                UPDATE learner_profiles 
                SET total_stars = %s 
                WHERE user_id = %s
            """, (total_stars, uid))
            
            # 6. Ensure practice attempts for both instruments
            for inst_id in [1, 2]:
                # get a valid lesson
                cursor.execute("SELECT lesson_id, title FROM lessons WHERE instrument_id = %s AND status = 'APPROVED' LIMIT 1", (inst_id,))
                first_lesson = cursor.fetchone()
                if not first_lesson: continue
                first_lid, lesson_title = first_lesson
                
                # Check if exercise exists for this lesson
                cursor.execute("SELECT id FROM exercises WHERE lesson_id = %s LIMIT 1", (first_lid,))
                ex_row = cursor.fetchone()
                if ex_row:
                    ex_id = ex_row[0]
                else:
                    # Create one
                    cursor.execute("""
                        INSERT INTO exercises (lesson_id, title, description, order_index, created_at, updated_at)
                        VALUES (%s, %s, 'Thực hành tự động tạo', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        RETURNING id
                    """, (first_lid, f"Thực hành: {lesson_title}"))
                    ex_id = cursor.fetchone()[0]
                
                # Insert practice_session
                cursor.execute("""
                    INSERT INTO practice_sessions (learner_id, started_at, ended_at, duration_minutes)
                    VALUES (%s, CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '30 minutes', 30)
                    RETURNING id
                """, (uid,))
                sess_id = cursor.fetchone()[0]
                
                # Insert practice_attempt
                client_uuid = str(uuid.uuid4())
                cursor.execute("""
                    INSERT INTO practice_attempts (
                        client_uuid, learner_id, exercise_id, session_id, started_at, completed_at, created_at,
                        pitch_score, rhythm_score, total_score, stars, points_earned, sync_status
                    ) VALUES (
                        %s, %s, %s, %s, CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP,
                        100, 100, 100, 3, 50, 'SYNCED'
                    ) RETURNING id
                """, (client_uuid, uid, ex_id, sess_id))
                attempt_id = cursor.fetchone()[0]
                
                # We can also seed instructor_feedback if needed, but not strictly asked, just "instructor đọc được và comment" 
                # -> they just need the attempt to exist so they can comment on it themselves.

        conn.commit()
        print("Successfully fulfilled all user requests for both accounts!")
        
        cursor.close()
        conn.close()
    except Exception as e:
        print("Error:", e)

if __name__ == "__main__":
    main()
