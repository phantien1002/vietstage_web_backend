import psycopg2

def main():
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    conn = psycopg2.connect(url)
    cursor = conn.cursor()
    
    try:
        cursor.execute("ALTER TABLE quizzes ADD COLUMN IF NOT EXISTS instrument_id bigint;")
        cursor.execute("ALTER TABLE quizzes ALTER COLUMN lesson_id DROP NOT NULL;")
        
        cursor.execute("ALTER TABLE quizzes DROP CONSTRAINT IF EXISTS fk_quiz_instrument;")
        cursor.execute("ALTER TABLE quizzes ADD CONSTRAINT fk_quiz_instrument FOREIGN KEY (instrument_id) REFERENCES instruments(id);")
        
        conn.commit()
        print("Migration for quizzes table successful.")
        
    except Exception as e:
        conn.rollback()
        print("Error:", e)
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    main()
