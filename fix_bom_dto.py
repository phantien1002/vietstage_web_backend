import os
import codecs

filepath = 'src/main/java/com/example/vietstage_web_be/dto/request/LessonVisibilityRequest.java'
with open(filepath, 'rb') as f:
    content = f.read()

if content.startswith(codecs.BOM_UTF8):
    content = content[len(codecs.BOM_UTF8):]
    with open(filepath, 'wb') as f:
        f.write(content)
