import os
import re

impl_path = 'src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java'
with open(impl_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add repository injection
if 'LessonActivityRepository lessonActivityRepository' not in content:
    content = content.replace(
        'private final ILessonContentService lessonContentService;',
        'private final ILessonContentService lessonContentService;\n    private final com.example.vietstage_web_be.repository.LessonActivityRepository lessonActivityRepository;'
    )

new_method_impl = """
    @Override
    public com.example.vietstage_web_be.dto.response.LessonConfigResponse getLessonConfig(String lessonCode, Integer revision) {
        java.util.List<com.example.vietstage_web_be.entity.LessonActivity> activities = lessonActivityRepository.findByLessonCodeAndRevisionOrderByOrderIndexAsc(lessonCode, revision);
        
        java.util.List<com.example.vietstage_web_be.dto.response.LessonConfigResponse.ActivityConfigDto> dtos = activities.stream().map(a -> 
            com.example.vietstage_web_be.dto.response.LessonConfigResponse.ActivityConfigDto.builder()
                .activityCode(a.getActivityCode())
                .activityType(a.getActivityType())
                .orderIndex(a.getOrderIndex())
                .contentText(a.getContentText())
                .config(a.getConfig())
                .build()
        ).collect(java.util.stream.Collectors.toList());

        return com.example.vietstage_web_be.dto.response.LessonConfigResponse.builder()
                .lessonCode(lessonCode)
                .revision(revision)
                .activities(dtos)
                .build();
    }
"""

content = re.sub(r'public com\.example\.vietstage_web_be\.dto\.response\.LessonConfigResponse getLessonConfig.*?}', new_method_impl.strip(), content, flags=re.DOTALL)

with open(impl_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Implemented getLessonConfig fully")
