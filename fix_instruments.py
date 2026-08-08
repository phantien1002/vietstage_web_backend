import os

files_to_update = [
    'vietstage.sql',
    'vietstage_full_reset.sql',
    'vietstage_full_reset_v2.sql',
    'seed_data.sql'
]

replacements = {
    "'Dan Tranh'": "'Đàn Tranh'",
    "'Dan Bau'": "'Đàn Bầu'",
    "'Sao Truc'": "'Sáo'",
    "'Trong'": "'Trống Chầu'"
}

for filename in files_to_update:
    if not os.path.exists(filename):
        continue
    
    with open(filename, 'r', encoding='utf-8') as f:
        content = f.read()
        
    for old, new in replacements.items():
        content = content.replace(old, new)
        
    with open(filename, 'w', encoding='utf-8') as f:
        f.write(content)

print("Updated successfully")
