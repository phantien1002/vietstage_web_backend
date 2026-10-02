import json
import uuid

def escape_sql(val):
    if val is None:
        return 'NULL'
    if isinstance(val, bool):
        return 'true' if val else 'false'
    if isinstance(val, (int, float)):
        return str(val)
    if isinstance(val, str):
        # Escape single quotes
        val = val.replace("'", "''")
        return f"'{val}'"
    # For lists/dicts, convert to json
    json_str = json.dumps(val, ensure_ascii=False).replace("'", "''")
    return f"'{json_str}'::jsonb"

def generate_sql():
    with open('parsed_data.json', 'r', encoding='utf-8') as f:
        data = json.load(f)

    # We need to map instrument_id and skill_level_id. 
    # DanTranh instrument_id = 1, SaoTruc = 2
    # Skill level 1-6 map to skill_level_id = 1-6
    
    sql_statements = []
    sql_statements.append("BEGIN;")
    sql_statements.append("TRUNCATE TABLE exercises CASCADE;")
    sql_statements.append("TRUNCATE TABLE lesson_contents CASCADE;")
    sql_statements.append("TRUNCATE TABLE assets CASCADE;")
    sql_statements.append("TRUNCATE TABLE lessons CASCADE;")
    
    # Let's map lessons
    # dt_data has LESSON_DIALOGUES, dt_course has ROADMAP (we don't have ROADMAP due to parse error)
    # wait, we didn't parse ALL_LESSONS correctly? 
    # Let's check what keys we have.
    dt_data = data.get("DanTranhBundledLessonData", {})
    st_data = data.get("SaoTrucBundledLessonData", {})
    
    all_lessons = []
    if "ALL_LESSONS" in dt_data:
        for lesson in dt_data["ALL_LESSONS"]:
            lesson["instrument"] = 1
            all_lessons.append(lesson)
    if "ALL_LESSONS" in st_data:
        for lesson in st_data["ALL_LESSONS"]:
            lesson["instrument"] = 2
            all_lessons.append(lesson)
            
    # Keep track of IDs
    lesson_id_counter = 1
    content_id_counter = 1
    asset_id_counter = 1
    exercise_id_counter = 1
    
    for l in all_lessons:
        l_id = lesson_id_counter
        lesson_id_counter += 1
        
        lesson_code = l.get("id")
        title = l.get("title", "")
        description = l.get("note", "")
        instrument_id = l.get("instrument")
        skill_level_id = l.get("level", 1)
        video_path = l.get("video_path") or l.get("video") # Just some field
        
        # Insert lesson
        sql_statements.append(f"""
        INSERT INTO lessons (id, lesson_code, instrument_id, skill_level_id, created_by_user_id, title, description, approval_status, is_visible, order_index)
        VALUES ({l_id}, {escape_sql(lesson_code)}, {instrument_id}, {skill_level_id}, 1, {escape_sql(title)}, {escape_sql(description)}, 'APPROVED', true, {l_id});
        """)
        
        order_idx = 1
        
        # 1. Video Asset & Content
        if video_path and isinstance(video_path, str) and ("video" in video_path.lower() or "mp4" in video_path.lower() or "hướng dẫn" in video_path.lower() or "cầm sáo" in video_path.lower()):
            # Create asset
            a_id = asset_id_counter
            asset_id_counter += 1
            sql_statements.append(f"""
            INSERT INTO assets (id, file_path, file_type, file_size, uploaded_by_user_id)
            VALUES ({a_id}, {escape_sql(video_path)}, 'VIDEO', 0, 1);
            """)
            
            # Create content
            c_id = content_id_counter
            content_id_counter += 1
            sql_statements.append(f"""
            INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index)
            VALUES ({c_id}, {l_id}, 'VIDEO_CUE', {a_id}, {order_idx});
            """)
            order_idx += 1
            
        # 2. Teacher Dialogues
        # We need to look up dialogues
        dialogues = []
        if instrument_id == 1 and "LESSON_DIALOGUES" in dt_data:
            dialogues = dt_data["LESSON_DIALOGUES"].get(lesson_code, [])
        elif instrument_id == 2 and "LESSON_DIALOGUES" in st_data:
            dialogues = st_data["LESSON_DIALOGUES"].get(lesson_code, [])
            
        for d in dialogues:
            c_id = content_id_counter
            content_id_counter += 1
            sql_statements.append(f"""
            INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index)
            VALUES ({c_id}, {l_id}, 'TEACHER_SPEECH', {escape_sql(d)}, {order_idx});
            """)
            order_idx += 1
            
        # 3. Practice
        practice_data = None
        if instrument_id == 1 and "PRACTICE_CONFIG" in dt_data:
            practice_data = dt_data["PRACTICE_CONFIG"].get(lesson_code)
        elif instrument_id == 2 and "PRACTICE_CONFIG" in st_data:
            practice_data = st_data["PRACTICE_CONFIG"].get(lesson_code)
            
        if practice_data:
            e_id = exercise_id_counter
            exercise_id_counter += 1
            sql_statements.append(f"""
            INSERT INTO exercises (id, lesson_id, title, description, exercise_type, practice_mode, pass_threshold, config_json, order_index)
            VALUES ({e_id}, {l_id}, {escape_sql(title + ' - Thực hành')}, 'Thực hành nhạc cụ', 'PRACTICE', 'PITCH_DETECTION', 80, {escape_sql(practice_data)}, {order_idx});
            """)
            order_idx += 1

    sql_statements.append("COMMIT;")
    
    with open('seed.sql', 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_statements))

if __name__ == '__main__':
    generate_sql()
