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

try:
    cur.execute("DELETE FROM lessons WHERE instrument_id = %s", (instrument_id,))
    print("Deleted all remaining lessons successfully!")
except Exception as e:
    print(f"Delete failed: {e}")
