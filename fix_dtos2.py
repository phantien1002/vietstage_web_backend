import os
import re

def replace_in_file(filepath, old_str, new_str):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        content = content.replace(old_str, new_str)
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Replaced in {filepath}")
    except FileNotFoundError:
        pass

replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', '.visibilityStatus(', '.isVisible(true) // was ')
replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', '.reviewStatus(', '.approvalStatus("DRAFT") // was ')
replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', '.approvalStatus(', '.approvalStatus("DRAFT") // was ') # To fix the double replacement I made earlier

replace_in_file('src/main/java/com/example/vietstage_web_be/dto/response/LessonResponse.java', 'private String reviewStatus;', 'private String approvalStatus;')
replace_in_file('src/main/java/com/example/vietstage_web_be/dto/response/LessonResponse.java', 'private String visibilityStatus;', 'private Boolean isVisible;')

replace_in_file('src/main/java/com/example/vietstage_web_be/dto/request/LessonRequest.java', 'private String reviewStatus;', 'private String approvalStatus;')
replace_in_file('src/main/java/com/example/vietstage_web_be/dto/request/LessonRequest.java', 'private String visibilityStatus;', 'private Boolean isVisible;')

replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', 'lesson.getVisibilityStatus()', 'lesson.getIsVisible() != null && lesson.getIsVisible() ? "PUBLIC" : "HIDDEN"')
replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', 'lesson.getReviewStatus()', 'lesson.getApprovalStatus()')

