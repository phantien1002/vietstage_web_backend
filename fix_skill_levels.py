import json
import psycopg2

def main():
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    conn = psycopg2.connect(url)
    cursor = conn.cursor()
    
    with open('parsed_data.json', 'r', encoding='utf-8') as f:
        parsed_data = json.load(f)
        
    dt_levels = parsed_data.get('DanTranhBundledLessonData', {}).get('LEVELS', [])
    
    # We will fetch all Dan Tranh lessons from DB ordered by order_index
    cursor.execute("SELECT lesson_id, order_index FROM lessons WHERE instrument_id = 1 ORDER BY order_index ASC;")
    db_lessons = cursor.fetchall()
    
    # Build a list of skill_level_ids based on the original JSON levels
    expected_skill_levels = []
    for level_idx, level in enumerate(dt_levels):
        # User says: Level 1 -> Cơ bản (1), Level 2 -> Trung cấp (2), Level 3 -> Nâng cao (3).
        # Assuming level_idx 0 is 1, 1 is 2, and 2+ is 3.
        skill_level_id = 1 if level_idx == 0 else (2 if level_idx == 1 else 3)
        for _ in level.get('lessons', []):
            expected_skill_levels.append(skill_level_id)
            
    if len(db_lessons) != len(expected_skill_levels):
        print(f"Count mismatch! DB has {len(db_lessons)}, JSON has {len(expected_skill_levels)}")
        return
        
    updates = 0
    for db_lesson, expected_skill in zip(db_lessons, expected_skill_levels):
        lesson_id = db_lesson[0]
        cursor.execute("UPDATE lessons SET skill_level_id = %s WHERE lesson_id = %s;", (expected_skill, lesson_id))
        updates += 1
        
    conn.commit()
    cursor.close()
    conn.close()
    
    print(f"Successfully updated skill_level_id for {updates} Dan Tranh lessons.")

if __name__ == '__main__':
    main()
