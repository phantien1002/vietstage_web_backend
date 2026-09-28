import os
import re

base_dir = r"d:\Do_An\vietstage_web_backend\src\main\java\com\example\vietstage_web_be"

def clean_dangling_private(rel_path):
    path = os.path.join(base_dir, rel_path)
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
        
        # Remove lines that just contain `private ` (and maybe whitespace/annotations before it that refer to unlockType)
        # Actually in UpdateCosmeticRequest, it's:
        # @Pattern(...)
        # private 
        content = re.sub(r'@Pattern\(regexp = "\^\(STARS\|ACHIEVEMENT\|DEFAULT\)\$", message = "Loại mở khóa không hợp lệ"\)\s*private\s*', '', content)
        content = re.sub(r'@Pattern\(regexp = "\^\(STARS\|ACHIEVEMENT\|DEFAULT\)\$", message = "Loi mY khA3a khA\'ng hp l"\)\s*private\s*', '', content)
        
        # In CreateCosmeticRequest, it might be similar. Just remove dangling "private" with whitespace
        content = re.sub(r'^[ \t]*private\s*$', '', content, flags=re.MULTILINE)
        content = re.sub(r'^[ \t]*@Pattern\([^)]*\)\s*private\s*$', '', content, flags=re.MULTILINE)
        
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)

dtos = [
    "dto/request/CreateCosmeticRequest.java",
    "dto/request/UpdateCosmeticRequest.java",
    "dto/response/CosmeticItemResponse.java",
    "dto/response/LearnerCosmeticResponse.java"
]

for d in dtos:
    clean_dangling_private(d)

print("Cleaned dangling private modifiers.")
