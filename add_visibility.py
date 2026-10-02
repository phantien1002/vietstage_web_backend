import os
import re

service_interface_path = 'src/main/java/com/example/vietstage_web_be/service/IAdminReviewService.java'
with open(service_interface_path, 'r', encoding='utf-8') as f:
    content = f.read()

if 'void updateLessonVisibility' not in content:
    content = content.replace('}', '    void updateLessonVisibility(Long id, com.example.vietstage_web_be.dto.request.LessonVisibilityRequest request, Long adminId);\n}')
    with open(service_interface_path, 'w', encoding='utf-8') as f:
        f.write(content)


service_impl_path = 'src/main/java/com/example/vietstage_web_be/service/impl/AdminReviewServiceImpl.java'
with open(service_impl_path, 'r', encoding='utf-8') as f:
    content = f.read()

new_method = """
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateLessonVisibility(Long id, com.example.vietstage_web_be.dto.request.LessonVisibilityRequest request, Long adminId) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND, "Lesson not found"));
        
        lesson.setIsVisible(request.getIsVisible());
        if (!request.getIsVisible()) {
            lesson.setHiddenAt(java.time.LocalDateTime.now());
            lesson.setHiddenReason(request.getHiddenReason());
        } else {
            lesson.setHiddenAt(null);
            lesson.setHiddenReason(null);
        }
        
        lessonRepository.save(lesson);
        // Optional: log visibility change or notify instructor
    }
"""

if 'public void updateLessonVisibility' not in content:
    content = content.replace('}', new_method + '\n}')
    with open(service_impl_path, 'w', encoding='utf-8') as f:
        f.write(content)

controller_path = 'src/main/java/com/example/vietstage_web_be/controller/AdminController.java'
with open(controller_path, 'r', encoding='utf-8') as f:
    content = f.read()

new_endpoint = """
    @PutMapping("/lessons/{id}/visibility")
    @Operation(summary = "?n/hi?n b?i h?c (ADMIN)")
    public ApiResponse<Void> updateLessonVisibility(
            @AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User currentUser,
            @PathVariable Long id,
            @Valid @RequestBody com.example.vietstage_web_be.dto.request.LessonVisibilityRequest request) {
        
        adminReviewService.updateLessonVisibility(id, request, currentUser.getId());
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Successfully updated lesson visibility")
                .build();
    }
"""
if 'updateLessonVisibility' not in content:
    content = content.replace('}', new_endpoint + '\n}')
    with open(controller_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Added updateLessonVisibility to AdminController and AdminReviewService")
