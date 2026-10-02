import os
import codecs

directory = 'src/main/java/com/example/vietstage_web_be/repository'
files_to_fix = [
    'LessonLegacyKeyRepository.java', 'LessonRevisionRepository.java',
    'LessonActivityRepository.java', 'ExerciseConfigRepository.java',
    'CompletionAttemptRepository.java'
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
