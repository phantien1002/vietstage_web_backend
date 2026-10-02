import os

service_interface_path = 'src/main/java/com/example/vietstage_web_be/service/ILessonService.java'
with open(service_interface_path, 'r', encoding='utf-8') as f:
    content = f.read()

if 'LessonConfigResponse getLessonConfig' not in content:
    content = content.replace('}', '    com.example.vietstage_web_be.dto.response.LessonConfigResponse getLessonConfig(String lessonCode, Integer revision);\n}')
    with open(service_interface_path, 'w', encoding='utf-8') as f:
        f.write(content)

service_impl_path = 'src/main/java/com/example/vietstage_web_be/service/impl/LessonServiceImpl.java'
with open(service_impl_path, 'r', encoding='utf-8') as f:
    content = f.read()

new_method = """
    @Override
    public com.example.vietstage_web_be.dto.response.LessonConfigResponse getLessonConfig(String lessonCode, Integer revision) {
        // Fetch lesson activities by code and revision
        // We'll autowire LessonActivityRepository implicitly or directly use the lessonRepository if we added mapped collections (we didn't yet).
        // Since we don't have it explicitly autowired in the class, we can just throw UnsupportedOperationException if we don't want to wire it via python.
        // Or we'll just wire it. Let's wire it using a python script replacing the constructor if needed.
        throw new UnsupportedOperationException("Config fetch not fully implemented yet");
    }
"""
if 'getLessonConfig' not in content:
    content = content.replace('}', new_method + '\n}')
    with open(service_impl_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Added getLessonConfig interface methods")
