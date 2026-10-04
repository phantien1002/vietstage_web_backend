import psycopg2
conn = psycopg2.connect('postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require')
cursor = conn.cursor()
cursor.execute("UPDATE lessons SET approval_status = 'APPROVED', is_visible = true WHERE approval_status = 'DRAFT';")
conn.commit()
print(cursor.rowcount, 'rows updated to APPROVED')
cursor.close()
conn.close()
