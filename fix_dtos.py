import os

def replace_in_file(filepath, old_str, new_str):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    content = content.replace(old_str, new_str)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

# Update LessonServiceImpl
replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', '.visibilityStatus(', '.isVisible(true) // was ')
replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', '.approvalStatus(', '.approvalStatus("DRAFT") // was ')

# Check DTOs
replace_in_file('src/main/java/com/example/vietstage_web_be/dto/response/LessonResponse.java', 'private String reviewStatus;', 'private String approvalStatus;')
replace_in_file('src/main/java/com/example/vietstage_web_be/dto/response/LessonResponse.java', 'private String visibilityStatus;', 'private Boolean isVisible;')

replace_in_file('src/main/java/com/example/vietstage_web_be/dto/request/LessonRequest.java', 'private String reviewStatus;', 'private String approvalStatus;')
replace_in_file('src/main/java/com/example/vietstage_web_be/dto/request/LessonRequest.java', 'private String visibilityStatus;', 'private Boolean isVisible;')

replace_in_file('src/main/java/com/example/vietstage_web_be/dto/request/LessonUpdateRequest.java', 'private String reviewStatus;', 'private String approvalStatus;')
replace_in_file('src/main/java/com/example/vietstage_web_be/dto/request/LessonUpdateRequest.java', 'private String visibilityStatus;', 'private Boolean isVisible;')

print("Fixed DTOs and remaining builder errors")
