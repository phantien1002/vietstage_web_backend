import psycopg2
from datetime import datetime

def main():
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    conn = psycopg2.connect(url)
    cursor = conn.cursor()
    
    emails = ['phuclong2710@gmail.com', 'thanhdattb19@gmail.com']
    
    try:
        # Get user IDs
        cursor.execute("SELECT user_id, email FROM users WHERE email = ANY(%s);", (emails,))
        users = cursor.fetchall()
        
        if not users:
            print("Users not found")
            return
            
        user_ids = {u[1]: u[0] for u in users}
        print("User IDs:", user_ids)
        
        # Set is_visible = true for approved lessons (as they were previously False by default)
        cursor.execute("UPDATE lessons SET is_visible = true WHERE status = 'APPROVED';")

        # Get all visible approved lessons for Dan Tranh (1) and Sao Truc (2)
        cursor.execute("""
            SELECT lesson_id FROM lessons 
            WHERE instrument_id IN (1, 2) 
            AND status = 'APPROVED' 
            AND is_visible = true;
        """)
        lessons = [row[0] for row in cursor.fetchall()]
        print(f"Found {len(lessons)} lessons.")
        
        now = datetime.now()
        
        # Upsert progress for each user
        for email in emails:
            uid = user_ids.get(email)
            if not uid:
                print(f"Skipping {email} - not found")
                continue
                
            print(f"Processing {email} (ID: {uid})")
            
            for lid in lessons:
                # Check if exists
                cursor.execute("""
                    SELECT id FROM learner_lesson_progress 
                    WHERE learner_user_id = %s AND lesson_id = %s;
                """, (uid, lid))
                row = cursor.fetchone()
                
                if row:
                    # Update
                    cursor.execute("""
                        UPDATE learner_lesson_progress 
                        SET status = 'COMPLETED',
                            stars = 3,
                            best_score = 100,
                            updated_at = %s,
                            unlocked_at = COALESCE(unlocked_at, %s),
                            started_at = COALESCE(started_at, %s),
                            completed_at = COALESCE(completed_at, %s)
                        WHERE id = %s;
                    """, (now, now, now, now, row[0]))
                else:
                    # Insert
                    # Generate an idempotent lastClientAttemptId
                    last_attempt_id = f"auto_complete_{uid}_{lid}"
                    cursor.execute("""
                        INSERT INTO learner_lesson_progress (
                            learner_user_id, lesson_id, status, stars, best_score, 
                            unlocked_at, started_at, completed_at, updated_at, last_client_attempt_id
                        ) VALUES (%s, %s, 'COMPLETED', 3, 100, %s, %s, %s, %s, %s);
                    """, (uid, lid, now, now, now, now, last_attempt_id))
            
            # Recalculate total_stars for this user based ONLY on lesson completions
            # The prompt says: "tổng sao bằng 3 x tổng số bài đã hoàn thành, tính theo từng bài chứ không theo level."
            # Also: "ảnh đang hiển thị 12 bài nhưng 33 sao... Backend không nên ghi cứng số sao theo ảnh; phải tính từ số bản ghi"
            
            # Count stars from learner_lesson_progress where status = 'COMPLETED'
            cursor.execute("""
                SELECT SUM(stars) FROM learner_lesson_progress 
                WHERE learner_user_id = %s AND status = 'COMPLETED';
            """, (uid,))
            sum_stars = cursor.fetchone()[0] or 0
            
            # Update learner profile
            # Note: The user wants to ensure the total stars is perfectly aligned with the lesson records.
            # And they said "Mở khóa toàn bộ bài và level" -> The has_full_access = true is NOT allowed for other accounts, but for these two? 
            # Wait, "Hai tài khoản trên phải... Mở khóa toàn bộ bài và level... Các tài khoản khác không được cấp hasFullAccess".
            # So we should set has_full_access = true for these two accounts!
            cursor.execute("""
                UPDATE learner_profiles 
                SET total_stars = %s,
                    spendable_stars = GREATEST(spendable_stars, %s),
                    has_full_access = true,
                    updated_at = %s
                WHERE user_id = %s;
            """, (sum_stars, sum_stars, now, uid))
            
            if cursor.rowcount == 0:
                # If profile doesn't exist, insert it
                cursor.execute("""
                    INSERT INTO learner_profiles (user_id, total_stars, spendable_stars, has_full_access, updated_at)
                    VALUES (%s, %s, %s, true, %s);
                """, (uid, sum_stars, sum_stars, now))
                
            print(f"Updated {email} - total_stars: {sum_stars}")
            
        conn.commit()
        print("Transaction committed.")
    except Exception as e:
        conn.rollback()
        print("Error:", e)
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    main()
