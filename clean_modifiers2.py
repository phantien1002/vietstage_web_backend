import os
import re

base_dir = r"d:\Do_An\vietstage_web_backend\src\main\java\com\example\vietstage_web_be"

def clean_dangling_private(rel_path):
    path = os.path.join(base_dir, rel_path)
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
        
        # Remove lines that start with @Pattern... followed by 'private '
        # We will just remove any "private " followed immediately by another annotation like @jakarta
        # or anything that is clearly a dangling modifier
        content = re.sub(r'^[ \t]*@Pattern[^)]*\)\s*private\s*$', '', content, flags=re.MULTILINE)
        content = re.sub(r'^[ \t]*private\s*$', '', content, flags=re.MULTILINE)
        # Handle cases where the text has weird encoding
        content = re.sub(r'@Pattern\(regexp = "\^\(STARS\|ACHIEVEMENT\|DEFAULT\)\$".*?\n\s*private\s*', '', content)
        
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)

dtos = [
    "dto/request/CreateCosmeticRequest.java",
    "dto/request/UpdateCosmeticRequest.java"
]

for d in dtos:
    clean_dangling_private(d)

print("Cleaned dangling private modifiers.")
