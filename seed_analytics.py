import psycopg2
import random
from datetime import datetime, timedelta
import uuid

# Configuration
DB_URL = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
DEMO_EMAILS = ["phuclong2710@gmail.com", "thanhdattb19@gmail.com"]
DAYS_BACK = 60

def main():
    conn = psycopg2.connect(DB_URL)
    cursor = conn.cursor()
    
    # 1. Fetch Demo Users
    cursor.execute("SELECT user_id, email FROM users WHERE email = ANY(%s)", (DEMO_EMAILS,))
    users = cursor.fetchall()
    if not users:
        print("Demo users not found!")
        return
        
    print(f"Found {len(users)} demo users.")
    user_map = {row[1]: row[0] for row in users}
    
    # 2. Fetch Lessons and Exercises
    cursor.execute("SELECT l.lesson_id, l.instrument_id, e.id as exercise_id FROM lessons l JOIN exercises e ON l.lesson_id = e.lesson_id")
    lessons_exercises = cursor.fetchall()
    if not lessons_exercises:
        print("No lessons with exercises found!")
        return
        
    sao_truc_exercises = [row for row in lessons_exercises if row[1] == 3]
    dan_tranh_exercises = [row for row in lessons_exercises if row[1] == 2]
    
    print(f"Found {len(sao_truc_exercises)} Sao Truc exercises and {len(dan_tranh_exercises)} Dan Tranh exercises.")

    now = datetime.now()
    
    for email in DEMO_EMAILS:
        user_id = user_map.get(email)
        if not user_id:
            continue
            
        print(f"Seeding for user: {email} (ID: {user_id})")
        
        # Generate 1-3 sessions per day for the last 60 days
        for day in range(DAYS_BACK):
            current_date = now - timedelta(days=day)
            
            # 80% chance to be active on a given day to simulate retention
            if random.random() > 0.8:
                continue
                
            num_sessions = random.randint(1, 3)
            for _ in range(num_sessions):
                session_uuid = str(uuid.uuid4())
                start_hour = random.randint(8, 22)
                start_minute = random.randint(0, 59)
                started_at = current_date.replace(hour=start_hour, minute=start_minute, second=0, microsecond=0)
                duration_mins = random.randint(15, 60)
                ended_at = started_at + timedelta(minutes=duration_mins)
                
                # Insert Usage Session
                cursor.execute("""
                    INSERT INTO usage_sessions (usage_session_id, user_id, platform, started_at, ended_at, created_at)
                    VALUES (%s, %s, %s, %s, %s, %s)
                    ON CONFLICT (usage_session_id) DO NOTHING
                """, (session_uuid, user_id, "WEB", started_at, ended_at, started_at))
                
                # Choose random instrument for the session
                instrument_exercises = random.choice([sao_truc_exercises, dan_tranh_exercises])
                if not instrument_exercises:
                    continue
                    
                # Do 2-5 practice attempts in this session
                num_attempts = random.randint(2, 5)
                for _ in range(num_attempts):
                    ex = random.choice(instrument_exercises)
                    lesson_id, instr_id, exercise_id = ex
                    
                    attempt_start = started_at + timedelta(minutes=random.randint(1, duration_mins - 5))
                    attempt_duration = random.randint(1, 5)
                    attempt_end = attempt_start + timedelta(minutes=attempt_duration)
                    
                    # Score and stars
                    score = random.uniform(60.0, 100.0)
                    stars = 3 if score >= 90 else (2 if score >= 75 else 1)
                    
                    # Insert Practice Attempt
                    cursor.execute("""
                        INSERT INTO practice_attempts (client_uuid, learner_id, exercise_id, started_at, completed_at, total_score, pitch_score, rhythm_score, created_at, stars, points_earned, sync_status)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    """, (str(uuid.uuid4()), user_id, exercise_id, attempt_start, attempt_end, score, score, score, attempt_end, stars, int(score/10), 'SYNCED'))
                    
                    # Update/Insert learner_lesson_progress
                    cursor.execute("SELECT id, best_score FROM learner_lesson_progress WHERE learner_user_id = %s AND lesson_id = %s", (user_id, lesson_id))
                    progress_row = cursor.fetchone()
                    
                    if progress_row:
                        prog_id, best_score = progress_row
                        new_best = max(float(best_score) if best_score else 0.0, score)
                        cursor.execute("""
                            UPDATE learner_lesson_progress 
                            SET stars = 3, best_score = %s, completed_at = %s, updated_at = %s
                            WHERE id = %s
                        """, (new_best, attempt_end, attempt_end, prog_id))
                    else:
                        cursor.execute("""
                            INSERT INTO learner_lesson_progress (learner_user_id, lesson_id, status, stars, best_score, unlocked_at, started_at, completed_at, updated_at)
                            VALUES (%s, %s, 'COMPLETED', 3, %s, %s, %s, %s, %s)
                        """, (user_id, lesson_id, score, started_at - timedelta(days=1), attempt_start, attempt_end, attempt_end))

    conn.commit()
    cursor.close()
    conn.close()
    print("Seeding completed successfully.")

if __name__ == "__main__":
    main()
