import re

filepath = 'src/main/java/com/example/vietstage_web_be/entity/Lesson.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('@Column(name = "display_number", nullable = false)', '@Column(name = "display_number", nullable = false, columnDefinition = "varchar(255) default \'\'")')
content = content.replace('@Column(name = "legacy_level", nullable = false)', '@Column(name = "legacy_level", nullable = false, columnDefinition = "integer default 0")')
content = content.replace('@Column(name = "in_current_roadmap", nullable = false)', '@Column(name = "in_current_roadmap", nullable = false, columnDefinition = "boolean default false")')
content = content.replace('@Column(name = "approval_status", nullable = false)', '@Column(name = "approval_status", nullable = false, columnDefinition = "varchar(255) default \'DRAFT\'")')
content = content.replace('@Column(name = "is_visible", nullable = false)', '@Column(name = "is_visible", nullable = false, columnDefinition = "boolean default false")')
content = content.replace('@Column(name = "revision", nullable = false)', '@Column(name = "revision", nullable = false, columnDefinition = "integer default 1")')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Added columnDefinition defaults")
