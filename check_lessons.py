import psycopg2
conn = psycopg2.connect('postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require')
cursor = conn.cursor()
cursor.execute("SELECT COUNT(*) FROM lessons WHERE approval_status = 'APPROVED' AND is_visible = true;")
print("APPROVED & VISIBLE:", cursor.fetchone())
cursor.execute("SELECT COUNT(*) FROM lessons WHERE approval_status = 'APPROVED';")
print("APPROVED:", cursor.fetchone())
cursor.close()
conn.close()
