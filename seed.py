import json
import psycopg2

conn = psycopg2.connect(
    dbname="postgres",
    user="postgres.kzjdtnyxhnpqsfdprvrv",
    password="1000Vietstage",
    host="aws-0-ap-northeast-1.pooler.supabase.com",
    port="5432"
)
cur = conn.cursor()

# Get instrument_id
cur.execute("SELECT id FROM instruments WHERE instrument_code = 'dan_tranh' OR name ILIKE '%đàn tranh%'")
instrument_row = cur.fetchone()
if not instrument_row:
    cur.execute("INSERT INTO instruments (name, instrument_code, is_active) VALUES ('Đàn Tranh', 'dan_tranh', true) RETURNING id")
    instrument_id = cur.fetchone()[0]
else:
    instrument_id = instrument_row[0]

# Get skill levels
cur.execute("SELECT level_code, skill_level_id FROM skill_levels")
skill_level_map = {row[0]: row[1] for row in cur.fetchall()}
print("Skill levels map:", skill_level_map)

# If skill levels missing, insert them
for code in ['BEGINNER', 'INTERMEDIATE', 'ADVANCED']:
    if code not in skill_level_map:
        cur.execute("INSERT INTO skill_levels (level_code, level_name, order_index) VALUES (%s, %s, 1) RETURNING skill_level_id", (code, code))
        skill_level_map[code] = cur.fetchone()[0]

# Parse SQL inserts from 02_seed_snapshot.sql
import re
with open('Authoritative Input Files (Doc)/backend_handoff/dan_tranh/02_seed_snapshot.sql', 'r', encoding='utf-8') as f:
    sql_content = f.read()

pattern = r"INSERT INTO dan_tranh_review\.lesson\((.*?)\) VALUES \((.*?)\) ON CONFLICT DO NOTHING;"
for match in re.finditer(pattern, sql_content):
    cols = match.group(1).replace('skill_level_code', 'skill_level_id')
    cols += ", instrument_id"
    
    vals_str = match.group(2)
    
    # We need to parse vals_str properly. Let's use regex or split.
    # Format is: 'code','level','title',order,'display',legacy,bool,'{json}'::jsonb
    import ast
    parts = []
    current = ""
    in_str = False
    in_json = False
    i = 0
    while i < len(vals_str):
        if vals_str[i] == "'" and vals_str[i:i+2] != "''":
            if in_str:
                if vals_str[i:i+8] == "'::jsonb":
                    in_str = False
                    i += 7
                else:
                    in_str = False
            else:
                in_str = True
        elif vals_str[i] == "," and not in_str:
            parts.append(current)
            current = ""
            i += 1
            continue
            
        current += vals_str[i]
        i += 1
    parts.append(current)
    
    code = parts[0].strip("'")
    level = parts[1].strip("'")
    title = parts[2].strip("'")
    order = int(parts[3])
    display = parts[4].strip("'")
    legacy = int(parts[5])
    in_roadmap = parts[6].lower() == 'true'
    snapshot = parts[7][1:] if parts[7].startswith("'") else parts[7]
    if snapshot.endswith("'::jsonb"):
        snapshot = snapshot[:-8]
    elif snapshot.endswith("'"):
        snapshot = snapshot[:-1]
    
    # Replace unicode escapes? The sql has encoding issues (e.g. Luyn)
    # Actually, we should read source_snapshot.json to avoid encoding issues!
