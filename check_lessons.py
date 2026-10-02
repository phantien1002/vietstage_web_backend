import psycopg2

conn = psycopg2.connect(
    dbname="postgres",
    user="postgres.kzjdtnyxhnpqsfdprvrv",
    password="1000Vietstage",
    host="aws-0-ap-northeast-1.pooler.supabase.com",
    port="5432"
)
cur = conn.cursor()

cur.execute("SELECT lesson_code, title, order_index FROM lessons")
for row in cur.fetchall():
    print(row)
