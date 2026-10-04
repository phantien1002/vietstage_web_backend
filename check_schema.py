import psycopg2
DB_URL = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
conn = psycopg2.connect(DB_URL)
cursor = conn.cursor()
cursor.execute("SELECT column_name FROM information_schema.columns WHERE table_name = 'lessons'")
print("lessons:", cursor.fetchall())
cursor.execute("SELECT column_name FROM information_schema.columns WHERE table_name = 'exercises'")
print("exercises:", cursor.fetchall())
