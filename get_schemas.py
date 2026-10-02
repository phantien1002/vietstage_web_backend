import psycopg2
import json

conn=psycopg2.connect('postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require')
cursor=conn.cursor()
tables=['usage_sessions', 'lesson_activity', 'practice_attempts', 'learner_lesson_progress', 'users']
res={}
for t in tables:
    cursor.execute(f"SELECT column_name, data_type FROM information_schema.columns WHERE table_name = '{t}'")
    res[t] = cursor.fetchall()
print(json.dumps(res, indent=2))
