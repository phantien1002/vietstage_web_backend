import json
import psycopg2

def escape_sql(val):
    if val is None: return 'NULL'
    if isinstance(val, bool): return 'true' if val else 'false'
    if isinstance(val, (int, float)): return str(val)
    if isinstance(val, str):
        val = val.replace("'", "''")
        return f"'{val}'"
    return 'NULL'

def main():
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    conn = psycopg2.connect(url)
    cursor = conn.cursor()
    
    # 1. Clear old data
    cursor.execute("BEGIN;")
    cursor.execute("TRUNCATE TABLE exercises CASCADE;")
    cursor.execute("TRUNCATE TABLE lesson_contents CASCADE;")
    cursor.execute("TRUNCATE TABLE media_assets CASCADE;")
    cursor.execute("TRUNCATE TABLE lessons CASCADE;")
    with open('parsed_data.json', 'r', encoding='utf-8') as f:
        parsed_data = json.load(f)
        
    dt_data = parsed_data.get('DanTranhBundledLessonData', {})
    st_data = parsed_data.get('SaoTrucBundledLessonData', {})
    
    dt_levels = dt_data.get('LEVELS', [])
    st_lessons = st_data.get('ALL_LESSONS', [])
    
    # Insert Lessons
    order_idx = 1
    
    # Insert Dan Tranh (instrument_id = 1)
    for level_idx, level in enumerate(dt_levels):
        skill_level_id = 1 if level_idx == 0 else (2 if level_idx == 1 else 3)
        for l in level.get('lessons', []):
            title = l.get('title', 'Bài học')
            description = l.get('description', '')
            lesson_code = l.get('practice_id') or l.get('video_id') or title
            
            cursor.execute(f"""
                INSERT INTO lessons (lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, status, order_index)
                VALUES ({escape_sql(lesson_code)}, 1, {skill_level_id}, 1, {escape_sql(title)}, {escape_sql(description)}, 'APPROVED', {order_idx})
                RETURNING lesson_id;
            """)
            order_idx += 1
            
    # Insert Sao Truc (instrument_id = 2)
    # Group by skill levels? The source doesn't strictly define levels. Let's assume skill_level_id = 1 for all for now, or infer from title
    for l in st_lessons:
        title = l.get('title', 'Bài học')
        description = l.get('note', '')
        lesson_code = l.get('id', title)
        skill_level_id = 1
        
        cursor.execute(f"""
            INSERT INTO lessons (lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, status, order_index)
            VALUES ({escape_sql(lesson_code)}, 2, {skill_level_id}, 1, {escape_sql(title)}, {escape_sql(description)}, 'APPROVED', {order_idx})
            RETURNING lesson_id;
        """)
        order_idx += 1
        
    cursor.execute("COMMIT;")
    cursor.close()
    conn.close()
    print("Successfully inserted lessons into DB!")

if __name__ == '__main__':
    main()
