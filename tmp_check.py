import psycopg2

def main():
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    conn = psycopg2.connect(url)
    c = conn.cursor()
    c.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'practice_sessions';")
    print("practice_sessions:", c.fetchall())
    c.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'practice_attempts';")
    print("practice_attempts:", c.fetchall())
    c.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'exercises';")
    print("exercises:", c.fetchall())

if __name__ == '__main__':
    main()
