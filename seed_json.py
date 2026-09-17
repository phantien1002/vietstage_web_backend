import json
import psycopg2

conn = psycopg2.connect(
    dbname="postgres",
    user="postgres.kzjdtnyxhnpqsfdprvrv",
    password="1000Vietstage",
    host="aws-0-ap-northeast-1.pooler.supabase.com",
    port="5432"
)
conn.autocommit = True
cur = conn.cursor()

cur.execute("SELECT user_id FROM users LIMIT 1")
admin_user_id = cur.fetchone()[0]

cur.execute("SELECT id FROM instruments WHERE instrument_code = 'dan_tranh' OR name ILIKE '%đàn tranh%'")
instrument_id = cur.fetchone()[0]

cur.execute("SELECT level_code, skill_level_id FROM skill_levels")
skill_level_map = {row[0]: row[1] for row in cur.fetchall()}

with open('Authoritative Input Files (Doc)/backend_handoff/dan_tranh/source_snapshot.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

order_index_tracker = {}

inserted = 0
for lesson in data['lessons']:
    code = lesson['lessonCode']
    level_code = lesson.get('skillLevelCode') or 'ADVANCED'
    level_id = skill_level_map[level_code]
    title = lesson['title']
    
    # Track order index
    order = lesson['orderIndex']
    key = (instrument_id, level_id, order)
    if key in order_index_tracker:
        # Find next available
        while key in order_index_tracker:
            order += 1
            key = (instrument_id, level_id, order)
    order_index_tracker[key] = True
    
    display = lesson.get('displayNumber', '')
    legacy = lesson.get('legacyLevel', 0)
    in_roadmap = lesson.get('inCurrentRoadmap', False)
    snapshot = json.dumps(lesson.get('source', {}), ensure_ascii=False)
    
    cur.execute('''
        INSERT INTO lessons (
            lesson_code, skill_level_id, title, order_index, 
            display_number, legacy_level, in_current_roadmap, 
            source_snapshot, instrument_id, 
            approval_status, is_visible, revision, created_by_user_id
        ) VALUES (
            %s, %s, %s, %s, 
            %s, %s, %s, 
            %s, %s,
            'DRAFT', false, 1, %s
        ) ON CONFLICT (lesson_code) DO NOTHING
    ''', (code, level_id, title, order, display, legacy, in_roadmap, snapshot, instrument_id, admin_user_id))
    inserted += 1

print(f"Successfully processed {inserted} lessons.")
