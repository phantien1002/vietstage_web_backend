import psycopg2
db_url = 'postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres'
conn = psycopg2.connect(db_url)
conn.autocommit = True
cur = conn.cursor()
cur.execute("ALTER TABLE usage_sessions DROP CONSTRAINT ck_usage_platform;")
cur.execute("ALTER TABLE usage_sessions ADD CONSTRAINT ck_usage_platform CHECK (((platform)::text = ANY (ARRAY[('WINDOWS'::character varying)::text, ('ANDROID'::character varying)::text, ('IOS'::character varying)::text, ('WEB'::character varying)::text, ('GODOT'::character varying)::text])));")
print("Constraint updated successfully.")
