import os
import codecs

directory = 'src/main/java/com/example/vietstage_web_be/entity'
files_to_fix = [
    'Lesson.java', 'LessonLegacyKey.java', 'LessonRevision.java',
    'LessonActivity.java', 'ExerciseConfig.java', 'RuntimeDialogue.java',
    'RuntimeProfile.java', 'CompletionAttempt.java'
]

for filename in files_to_fix:
    path = os.path.join(directory, filename)
    with open(path, 'rb') as f:
        content = f.read()
    
    if content.startswith(codecs.BOM_UTF8):
        content = content[len(codecs.BOM_UTF8):]
        with open(path, 'wb') as f:
            f.write(content)
        print(f"Fixed BOM in {filename}")
    else:
        print(f"No BOM in {filename}")
