import psycopg2

def main():
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    conn = psycopg2.connect(url)
    cursor = conn.cursor()
    
    try:
        # Set all ROOM_DECOR items to ACTIVE
        cursor.execute("UPDATE cosmetic_items SET status = 'ACTIVE' WHERE item_type = 'ROOM_DECOR';")
        conn.commit()
        print("Updated existing ROOM_DECOR to ACTIVE.")
        
    except Exception as e:
        conn.rollback()
        print("Error:", e)
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    main()
