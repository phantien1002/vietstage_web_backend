import psycopg2
url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
conn = psycopg2.connect(url)
c = conn.cursor()
c.execute("SELECT table_name FROM information_schema.tables WHERE table_schema='public'")
print(c.fetchall())
