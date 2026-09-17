import psycopg2

conn = psycopg2.connect(
    dbname="postgres",
    user="postgres.kzjdtnyxhnpqsfdprvrv",
    password="1000Vietstage",
    host="aws-0-ap-northeast-1.pooler.supabase.com",
    port="5432"
)
conn.autocommit = True
cur = conn.cursor()

cur.execute("SELECT u.user_id, u.email, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id")
for row in cur.fetchall():
    print(row)
