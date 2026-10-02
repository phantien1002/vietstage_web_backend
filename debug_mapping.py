import json
import psycopg2
import sys
sys.stdout.reconfigure(encoding='utf-8')

def main():
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    conn = psycopg2.connect(url)
    cursor = conn.cursor()
    cursor.execute("SELECT lesson_code, title FROM lessons WHERE instrument_id=1")
    db_lessons = cursor.fetchall()
    
    with open('parsed_data.json', 'r', encoding='utf-8') as f:
        parsed_data = json.load(f)
    
    flat_lessons = [l for lvl in parsed_data['DanTranhBundledLessonData']['LEVELS'] for l in lvl['lessons']]
    
    for l in flat_lessons:
        if 'sheet' in l or 'practiceConfig' in l:
            # check if practice_id exists
            p_id = l.get('practice_id')
            v_id = l.get('video_id')
            print(f"Parsed: title={l.get('title')}, practice_id={p_id}, video_id={v_id}")
            
    print("--- DB ---")
    for lc, title in db_lessons:
        print(f"DB: title={title}, code={lc}")

if __name__ == '__main__':
    main()
