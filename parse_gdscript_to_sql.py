import json
import re

def parse_gdscript(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # We will split the file by 'const '
    parts = content.split('\nconst ')
    
    data = {}
    for part in parts[1:]: # skip the first part which is imports/extends
        # part looks like "NAME: Type = [...]" or "NAME := [...]" or "NAME = [...]"
        
        # Extract the name
        match = re.match(r'^([A-Za-z0-9_]+)', part)
        if not match:
            continue
        name = match.group(1)
        
        # Find the first '='
        eq_idx = part.find('=')
        if eq_idx == -1:
            continue
        
        value_str = part[eq_idx+1:].strip()
        
        # value_str goes until the end of the part
        # Let's clean it up for Python ast.literal_eval
        # Remove comments starting with #
        lines = []
        for line in value_str.split('\n'):
            line = re.sub(r'#.*', '', line)
            lines.append(line)
        value_str = '\n'.join(lines)
        
        # Replace true, false, null
        value_str = re.sub(r'\btrue\b', 'True', value_str)
        value_str = re.sub(r'\bfalse\b', 'False', value_str)
        value_str = re.sub(r'\bnull\b', 'None', value_str)
        
        import ast
        try:
            val = ast.literal_eval(value_str)
            data[name] = val
        except Exception as e:
            print(f"Could not parse {name}: {e}")
            
    return data

if __name__ == '__main__':
    base_dir = r"d:\Do_An\vietstage_web_backend\Authoritative Input Files (Doc)\vietstage_curriculum_backend_handoff\scripts"
    
    import os
    dt_data = parse_gdscript(os.path.join(base_dir, "DanTranhBundledLessonData.gd"))
    st_data = parse_gdscript(os.path.join(base_dir, "SaoTrucBundledLessonData.gd"))
    dt_course = parse_gdscript(os.path.join(base_dir, "DanTranhCourseData.gd"))
    st_course = parse_gdscript(os.path.join(base_dir, "SaoTrucCourseData.gd"))
    
    with open('parsed_data.json', 'w', encoding='utf-8') as f:
        json.dump({
            "DanTranhBundledLessonData": dt_data,
            "SaoTrucBundledLessonData": st_data,
            "DanTranhCourseData": dt_course,
            "SaoTrucCourseData": st_course
        }, f, ensure_ascii=False, indent=2)
    print("Done writing parsed_data.json")
