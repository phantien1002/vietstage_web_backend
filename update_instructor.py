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

cur.execute("SELECT id FROM instruments WHERE instrument_code = 'dan_tranh' OR name ILIKE '%đàn tranh%'")
instrument_id = cur.fetchone()[0]

cur.execute("SELECT user_id FROM users WHERE email = 'dantranh.master@vietstage.com'")
instructor_id = cur.fetchone()[0]

cur.execute("UPDATE lessons SET created_by_user_id = %s WHERE instrument_id = %s", (instructor_id, instrument_id))
print(f"Updated instructor ID to {instructor_id} for instrument {instrument_id}.")
