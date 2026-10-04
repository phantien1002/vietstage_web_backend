import psycopg2
DB_URL = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
conn = psycopg2.connect(DB_URL)
cursor = conn.cursor()
cursor.execute("SELECT count(*) FROM lessons")
print("lessons count:", cursor.fetchone()[0])
cursor.execute("SELECT count(*) FROM exercises")
print("exercises count:", cursor.fetchone()[0])
cursor.execute("SELECT count(*) FROM users")
print("users count:", cursor.fetchone()[0])
