import psycopg2
url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
conn = psycopg2.connect(url)
c = conn.cursor()

def check_cols(table):
    c.execute(f"SELECT column_name FROM information_schema.columns WHERE table_name = '{table}'")
    cols = [r[0] for r in c.fetchall()]
    print(f"{table} columns: {cols}")

check_cols('lessons')
check_cols('exercises')

c.execute("SELECT count(*) FROM lessons")
print("lessons count:", c.fetchone()[0])
c.execute("SELECT count(*) FROM exercises")
print("exercises count:", c.fetchone()[0])
