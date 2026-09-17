import os
import re

def replace_in_file(filepath, old_str, new_str):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    content = content.replace(old_str, new_str)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/AdminReviewServiceImpl.java', 'getReviewStatus()', 'getApprovalStatus()')
replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/AdminReviewServiceImpl.java', 'setReviewStatus(', 'setApprovalStatus(')
replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/AdminReviewServiceImpl.java', 'setVisibilityStatus(', 'setIsVisible(true); // ' )

replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', 'reviewStatus(', 'approvalStatus(')
replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', 'setReviewStatus(', 'setApprovalStatus(')
replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', 'getReviewStatus()', 'getApprovalStatus()')
replace_in_file('src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java', 'getVisibilityStatus()', 'getIsVisible() ? "PUBLIC" : "HIDDEN"')

print("Replaced status methods")
