import psycopg2
import sys

def main():
    sys.stdout.reconfigure(encoding='utf-8')
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    try:
        conn = psycopg2.connect(url)
        cursor = conn.cursor()

        cursor.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'practice_attempts';")
        cols = cursor.fetchall()
        for c in cols:
            print(f"{c[0]}: {c[1]}")

        cursor.close()
        conn.close()
    except Exception as e:
        print("Error:", e)

if __name__ == '__main__':
    main()
