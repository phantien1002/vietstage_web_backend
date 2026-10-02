import json
import psycopg2

def escape_sql(val):
    if val is None: return 'NULL'
    if isinstance(val, bool): return 'true' if val else 'false'
    if isinstance(val, (int, float)): return str(val)
    if isinstance(val, str):
        val = val.replace("'", "''")
        return f"'{val}'"
    json_str = json.dumps(val, ensure_ascii=False).replace("'", "''")
    return f"'{json_str}'::jsonb"

def main():
    # Connect to DB
    url = "postgresql://postgres.kzjdtnyxhnpqsfdprvrv:1000Vietstage@aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
    conn = psycopg2.connect(url)
    cursor = conn.cursor()
    cursor.execute("SELECT lesson_id, lesson_code, instrument_id, title FROM lessons")
    lessons = cursor.fetchall()
    
    # Get max IDs
    cursor.execute("SELECT COALESCE(MAX(id), 0) FROM exercises")
    exercise_id_counter = cursor.fetchone()[0] + 1
    cursor.execute("SELECT COALESCE(MAX(id), 0) FROM lesson_contents")
    content_id_counter = cursor.fetchone()[0] + 1
    cursor.execute("SELECT COALESCE(MAX(asset_id), 0) FROM media_assets")
    asset_id_counter = cursor.fetchone()[0] + 1
    
    with open('parsed_data.json', 'r', encoding='utf-8') as f:
        parsed_data = json.load(f)
        
    dt_data = parsed_data.get('DanTranhBundledLessonData', {})
    st_data = parsed_data.get('SaoTrucBundledLessonData', {})
    
    dt_dialogues = dt_data.get('LESSON_DIALOGUES', {})
    st_dialogues = st_data.get('LESSON_DIALOGUES', {})
    
    st_lessons_from_json = st_data.get('ALL_LESSONS', [])
    st_notes = st_data.get('LESSON_NOTES', {})
    st_durations = st_data.get('PRACTICE_SHEET_DURATIONS', {})
    st_fingerings = st_data.get('PRACTICE_FINGERINGS', {})
    st_freqs = st_data.get('PRACTICE_FREQS', {})
    
    st_video_paths = {}
    for sl in st_lessons_from_json:
        video = sl.get('video_path') or sl.get('video')
        if video:
            st_video_paths[sl.get('id')] = video

    dt_lessons_flat = []
    for level in dt_data.get('LEVELS', []):
        for l in level.get('lessons', []):
            dt_lessons_flat.append(l)

    sql = ["BEGIN;", "TRUNCATE TABLE exercises CASCADE;", "TRUNCATE TABLE lesson_contents CASCADE;", "TRUNCATE TABLE media_assets CASCADE;"]
    
    for l_id, lesson_code, instrument_id, lesson_title in lessons:
        order_idx = 1
        
        # 1. Video
        video_path = None
        practice_id = None
        dt_lesson = None
        if instrument_id == 1:
            for dtl in dt_lessons_flat:
                if dtl.get('title') == lesson_title or dtl.get('video_id') == lesson_code or dtl.get('practice_id') == lesson_code:
                    dt_lesson = dtl
                    video_path = dtl.get('video_path')
                    practice_id = dtl.get('practice_id') or lesson_code
                    break
        else:
            video_path = st_video_paths.get(lesson_code)
            practice_id = lesson_code

        if video_path and video_path.startswith('res://'):
            a_id = asset_id_counter
            asset_id_counter += 1
            sql.append(f"INSERT INTO media_assets (asset_id, lesson_id, asset_url, asset_type) VALUES ({a_id}, {l_id}, {escape_sql(video_path)}, 'TECHNIQUE_VIDEO');")
            sql.append(f"INSERT INTO lesson_contents (id, lesson_id, content_type, asset_id, order_index) VALUES ({content_id_counter}, {l_id}, 'VIDEO_CUE', {a_id}, {order_idx});")
            content_id_counter += 1
            order_idx += 1
            
        # 2. Teacher Speech
        dialogues = []
        if instrument_id == 1 and practice_id:
            dialogues = dt_dialogues.get(practice_id, [])
        elif instrument_id == 3:
            st_d = st_dialogues.get(lesson_code, {})
            if isinstance(st_d, dict):
                for k, v in st_d.items():
                    if isinstance(v, list):
                        dialogues.extend(v)
            elif isinstance(st_d, list):
                dialogues = st_d
            
        for dia in dialogues:
            text = dia.get('text')
            if text:
                sql.append(f"INSERT INTO lesson_contents (id, lesson_id, content_type, content_text, order_index) VALUES ({content_id_counter}, {l_id}, 'TEACHER_SPEECH', {escape_sql(text)}, {order_idx});")
                content_id_counter += 1
                order_idx += 1
                
        # 3. Exercise
        config_json = None
        practice_mode = 'PITCH_DETECTION'
        
        if instrument_id == 1 and dt_lesson:
            if 'practiceConfig' in dt_lesson:
                config_json = dt_lesson['practiceConfig']
                practice_mode = config_json.get('practiceMode', 'PITCH_DETECTION')
            elif 'sheet' in dt_lesson:
                config_json = {
                    'practiceMode': dt_lesson.get('practice_mode', 'note_sequence'),
                    'noteSequence': dt_lesson.get('sheet'),
                    'durations': dt_lesson.get('durations', []),
                    'fingerings': dt_lesson.get('fingerings', []),
                    'cues': dt_lesson.get('cues', [])
                }
                practice_mode = config_json.get('practiceMode', 'PITCH_DETECTION')
                
            if config_json and ('noteSequence' in config_json or 'rounds' in config_json or 'sheet' in dt_lesson):
                pass
            else:
                config_json = None
                
        elif instrument_id == 3:
            s_notes = st_notes.get(lesson_code)
            
            # Find in songs list
            st_songs = st_data.get('PRACTICE_SONGS_LIST', [])
            song_match = None
            lesson_note_field = None
            for sl in st_lessons_from_json:
                if sl.get('id') == lesson_code:
                    lesson_note_field = sl.get('note')
                    break
            
            for s in st_songs:
                if s.get('title') == lesson_note_field or s.get('title') == lesson_title:
                    song_match = s
                    break

            if s_notes and isinstance(s_notes, dict) and 'note' in s_notes:
                config_json = {
                    'practiceMode': 'note_sequence',
                    'noteSequence': [s_notes.get('note')],
                    'durations': [1.0],
                    'fingerings': [s_notes.get('fingers', [])]
                }
            elif song_match:
                config_json = {
                    'practiceMode': 'note_sequence',
                    'noteSequence': song_match.get('sheet', []),
                    'durations': song_match.get('durations', []),
                    'bpm': song_match.get('bpm', 100)
                }

        if config_json:
            e_id = exercise_id_counter
            exercise_id_counter += 1
            sql.append(f"INSERT INTO exercises (id, lesson_id, title, description, exercise_type, practice_mode, pass_threshold, config_json, order_index) VALUES ({e_id}, {l_id}, 'Thực hành', 'Thực hành', 'PRACTICE', {escape_sql(practice_mode)}, 80, {escape_sql(config_json)}::jsonb, {order_idx});")
            order_idx += 1

    sql.append("COMMIT;")
    
    with open('seed_new.sql', 'w', encoding='utf-8') as f:
        f.write("\n".join(sql))
        
    cursor.close()
    conn.close()
        
if __name__ == '__main__':
    main()
