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

cur.execute("SELECT user_id, email, full_name FROM users WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'INSTRUCTOR')")
instructors = cur.fetchall()

print("Available instructors:", instructors)
