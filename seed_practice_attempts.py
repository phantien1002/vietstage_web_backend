import psycopg2
from datetime import datetime, timedelta
import uuid

def main():
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    conn = psycopg2.connect(url)
    cursor = conn.cursor()
    
    emails = ['phuclong2710@gmail.com', 'thanhdattb19@gmail.com']
    
    try:
        # Get user IDs
        cursor.execute("SELECT user_id, email FROM users WHERE email = ANY(%s);", (emails,))
        users = cursor.fetchall()
        user_ids = {u[1]: u[0] for u in users}
        
        # Get one exercise for Dan Tranh
        cursor.execute("""
            SELECT e.id FROM exercises e 
            JOIN lessons l ON e.lesson_id = l.lesson_id 
            WHERE l.instrument_id = 1 LIMIT 1;
        """)
        dt_ex = cursor.fetchone()
        
        # Get one exercise for Sao Truc
        cursor.execute("""
            SELECT e.id FROM exercises e 
            JOIN lessons l ON e.lesson_id = l.lesson_id 
            WHERE l.instrument_id = 2 LIMIT 1;
        """)
        st_ex = cursor.fetchone()
        
        exercises = []
        if dt_ex:
            exercises.append(dt_ex[0])
            
        if st_ex:
            exercises.append(st_ex[0])
        else:
            # Create a mock exercise for Sao Truc
            cursor.execute("SELECT lesson_id FROM lessons WHERE instrument_id = 2 LIMIT 1;")
            st_lesson = cursor.fetchone()
            if st_lesson:
                cursor.execute("""
                    INSERT INTO exercises (lesson_id, title, description, order_index, created_at, updated_at)
                    VALUES (%s, 'Sáo Trúc Demo Exercise', 'Demo', 1, NOW(), NOW()) RETURNING id;
                """, (st_lesson[0],))
                st_new_ex = cursor.fetchone()[0]
                exercises.append(st_new_ex)
                print(f"Created mock exercise {st_new_ex} for Sao Truc.")
        
        if not exercises:
            print("No exercises found to seed attempts!")
            return
            
        now = datetime.now()
        
        for email in emails:
            uid = user_ids.get(email)
            if not uid: continue
            
            # Create practice session
            cursor.execute("""
                INSERT INTO practice_sessions (learner_id, started_at, ended_at, duration_minutes)
                VALUES (%s, %s, %s, %s) RETURNING id;
            """, (uid, now - timedelta(minutes=30), now, 30))
            session_id = cursor.fetchone()[0]
            
            # Create attempts
            for ex_id in exercises:
                # Check if attempt already exists to prevent duplicate demo seeds
                cursor.execute("""
                    SELECT id FROM practice_attempts WHERE learner_id = %s AND exercise_id = %s;
                """, (uid, ex_id))
                if cursor.fetchone():
                    print(f"Attempt already exists for {email}, exercise {ex_id}")
                    continue
                    
                client_uuid = str(uuid.uuid4())
                cursor.execute("""
                    INSERT INTO practice_attempts (
                        client_uuid, learner_id, exercise_id, session_id,
                        started_at, completed_at, created_at,
                        pitch_score, rhythm_score, total_score,
                        stars, points_earned, sync_status
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, 100, 100, 100, 3, 50, 'SYNCED');
                """, (client_uuid, uid, ex_id, session_id, now - timedelta(minutes=5), now, now))
                print(f"Created attempt for {email}, exercise {ex_id}")
                
        conn.commit()
        print("Done!")
    except Exception as e:
        conn.rollback()
        print("Error:", e)
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    main()
