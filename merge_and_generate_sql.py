import json
import re

def parse_sql_values():
    with open('src/main/resources/curriculum_seed_for_backend.sql', 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the VALUES block
    start_idx = content.find('VALUES')
    end_idx = content.find(')', start_idx)
    # Actually, it's multiple lines like ('...',...), ('...',...)
    # Let's just use regex to find all tuples
    
    pattern = re.compile(r"\s*\(\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*\)")
    
    lessons = []
    for match in pattern.finditer(content):
        lessons.append({
            'lesson_code': match.group(1),
            'instrument_name': match.group(2),
            'local_level': int(match.group(3)),
            'sequence_index': int(match.group(4)),
            'display_number': match.group(5),
            'title': match.group(6),
            'description': match.group(7),
            'teacher_text': match.group(8),
            'practice_config': match.group(9)
        })
        
    return lessons

def escape_sql(val):
    if val is None:
        return 'NULL'
    if isinstance(val, bool):
        return 'true' if val else 'false'
    if isinstance(val, (int, float)):
        return str(val)
    if isinstance(val, str):
        val = val.replace("'", "''")
        return f"'{val}'"
    json_str = json.dumps(val, ensure_ascii=False).replace("'", "''")
    return f"'{json_str}'::jsonb"

def generate_sql():
    lessons = parse_sql_values()
    with open('parsed_data.json', 'r', encoding='utf-8') as f:
        parsed_data = json.load(f)
        
    dt_dialogues = parsed_data.get('DanTranhBundledLessonData', {}).get('LESSON_DIALOGUES', {})
    st_dialogues = parsed_data.get('SaoTrucBundledLessonData', {}).get('LESSON_DIALOGUES', {})
    
    st_lessons_from_json = parsed_data.get('SaoTrucBundledLessonData', {}).get('ALL_LESSONS', [])
    # We might need video_path from ALL_LESSONS
    st_video_paths = {}
    for sl in st_lessons_from_json:
        video = sl.get('video_path') or sl.get('video')
        if video:
            st_video_paths[sl.get('id')] = video

    sql = ["BEGIN;", "TRUNCATE TABLE exercises CASCADE;", "TRUNCATE TABLE lesson_contents CASCADE;", "TRUNCATE TABLE media_assets CASCADE;", "TRUNCATE TABLE lessons CASCADE;"]
    
    lesson_id_counter = 1
    content_id_counter = 1
    asset_id_counter = 1
    exercise_id_counter = 1
    
    for l in lessons:
        l_id = lesson_id_counter
        lesson_id_counter += 1
        
        lesson_code = l['lesson_code']
        instrument_id = 1 if l['instrument_name'] == 'Đàn Tranh' else 2
        
        sql.append(f"""
        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES ({l_id}, {escape_sql(lesson_code)}, {instrument_id}, {l['local_level']}, 1, {escape_sql(l['title'])}, {escape_sql(l['description'])}, 'APPROVED', true, {l_id});
        """)
        
        order_idx = 1
        
        # Check if there is a video
        # In the old sql, some practice configs have "video" inside configJson, or there is a specific lesson.
        # But for SaoTruc, videos were in st_video_paths.
        video_path = st_video_paths.get(lesson_code)
        if instrument_id == 2 and video_path:
            a_id = asset_id_counter
            asset_id_counter += 1
            sql.append(f"""
            INSERT INTO media_assets (id, file_path, file_type, file_size, uploaded_by_user_id)
            VALUES ({a_id}, {escape_sql(video_path)}, 'VIDEO', 0, 1);
            """)
            sql.append(f"""
            INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index)
            VALUES ({content_id_counter}, {l_id}, 'VIDEO_CUE', {a_id}, {order_idx});
            """)
            content_id_counter += 1
            order_idx += 1
            
        # Teacher dialogues
        dialogues = []
        if instrument_id == 1:
            dialogues = dt_dialogues.get(lesson_code, [])
        else:
            dialogues = st_dialogues.get(lesson_code, [])
            
        if dialogues:
            for d in dialogues:
                text = d.get('text', '') if isinstance(d, dict) else (d if isinstance(d, str) else str(d))
                if text:
                    sql.append(f"""
                    INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
                    VALUES ({content_id_counter}, {l_id}, 'TEACHER_SPEECH', {escape_sql(text)}, {order_idx});
                    """)
                    content_id_counter += 1
                    order_idx += 1
        else:
            # If no dialogues in .gd, use teacher_text from old sql if it exists
            if l['teacher_text']:
                sql.append(f"""
                INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
                VALUES ({content_id_counter}, {l_id}, 'TEACHER_SPEECH', {escape_sql(l['teacher_text'])}, {order_idx});
                """)
                content_id_counter += 1
                order_idx += 1
                
        # Exercises
        if l['practice_config']:
            # The original SQL had practice_config as JSON string.
            config_json = l['practice_config']
            e_id = exercise_id_counter
            exercise_id_counter += 1
            sql.append(f"""
            INSERT INTO exercises (id, lesson_id, title, description, exercise_type, practice_mode, pass_threshold, config_json, order_index)
            VALUES ({e_id}, {l_id}, {escape_sql(l['title'] + ' - Thực hành')}, 'Thực hành', 'PRACTICE', 'PITCH_DETECTION', 80, {escape_sql(config_json)}::jsonb, {order_idx});
            """)
            order_idx += 1

    sql.append("COMMIT;")
    
    with open('seed_new.sql', 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql))

if __name__ == '__main__':
    generate_sql()
