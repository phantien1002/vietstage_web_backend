import psycopg2
import os

url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres?sslmode=require"
# Note: pooler port is usually 6543 for transaction mode, or 5432 for session mode
# The application.properties uses 5432. Let's stick with 5432.
url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"

with open("seed_new.sql", "r", encoding="utf-8") as f:
    sql = f.read()

try:
    conn = psycopg2.connect(url)
    conn.autocommit = True
    cursor = conn.cursor()
    cursor.execute(sql)
    print("Seed executed successfully.")
except Exception as e:
    print("Error:", e)
finally:
    if 'cursor' in locals():
        cursor.close()
    if 'conn' in locals():
        conn.close()
