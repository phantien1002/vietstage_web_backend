import os
import re

interface_path = r"d:\Do_An\vietstage_web_backend\src\main\java\com\example\vietstage_web_be\service\ILearnerProgressService.java"
impl_path = r"d:\Do_An\vietstage_web_backend\src\main\java\com\example\vietstage_web_be\service\impl\LearnerProgressServiceImpl.java"
controller_path = r"d:\Do_An\vietstage_web_backend\src\main\java\com\example\vietstage_web_be\controller\AppCourseController.java"

# 1. Update interface
with open(interface_path, "r", encoding="utf-8") as f:
    interface_content = f.read()
if "getCourseProgress" not in interface_content:
    interface_content = interface_content.replace(
        "com.example.vietstage_web_be.dto.response.LessonCompletionResponse completeLesson(Long learnerId, Long lessonId, com.example.vietstage_web_be.dto.request.LessonCompletionRequest request);",
        "com.example.vietstage_web_be.dto.response.LessonCompletionResponse completeLesson(Long learnerId, Long lessonId, com.example.vietstage_web_be.dto.request.LessonCompletionRequest request);\n\n    com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse getCourseProgress(Long learnerId);"
    )
    with open(interface_path, "w", encoding="utf-8") as f:
        f.write(interface_content)

# 2. Update Implementation
impl_code = """
    @Override
    public com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse getCourseProgress(Long learnerId) {
        LearnerProfile profile = learnerProfileRepository.findByUserId(learnerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Learner profile not found: " + learnerId));
        
        boolean hasFullAccess = Boolean.TRUE.equals(profile.getHasFullAccess());
        
        List<Lesson> allLessons = lessonRepository.findAll();
        List<LessonCompletion> completions = lessonCompletionRepository.findByLearnerId(learnerId);
        
        java.util.Map<Long, LessonCompletion> completionMap = new java.util.HashMap<>();
        for (LessonCompletion c : completions) {
            completionMap.put(c.getLesson().getId(), c);
        }
        
        List<com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse.LearnerLessonProgressDTO> lessonDtos = new java.util.ArrayList<>();
        List<com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse.LearnerLevelProgressDTO> levelDtos = new java.util.ArrayList<>();
        
        java.util.Set<Long> levelIds = new java.util.HashSet<>();
        
        for (Lesson lesson : allLessons) {
            LessonCompletion completion = completionMap.get(lesson.getId());
            String status = completion != null && completion.getStatus() != null ? completion.getStatus() : "NOT_STARTED";
            
            if ("LOCKED".equals(status)) {
                status = "NOT_STARTED";
            }
            
            boolean isUnlocked = hasFullAccess || checkIsUnlocked(lesson, learnerId);
            
            lessonDtos.add(com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse.LearnerLessonProgressDTO.builder()
                    .lessonId(lesson.getId())
                    .isUnlocked(isUnlocked)
                    .learningStatus(isUnlocked ? status : "LOCKED")
                    .completed(completion != null ? completion.getCompleted() : false)
                    .completedAt(completion != null ? completion.getCompletedAt() : null)
                    .lessonStars(completion != null ? completion.getStars() : 0)
                    .highestScore(completion != null ? completion.getBestScore() : null)
                    .build());
                    
            if (lesson.getSkillLevel() != null && !levelIds.contains(lesson.getSkillLevel().getId())) {
                levelIds.add(lesson.getSkillLevel().getId());
                // Level status logic is simplified for now (can be computed by aggregating lesson statuses)
                levelDtos.add(com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse.LearnerLevelProgressDTO.builder()
                        .levelId(lesson.getSkillLevel().getId())
                        .isUnlocked(hasFullAccess || true) // Simplified
                        .learningStatus("IN_PROGRESS") // Simplified
                        .build());
            }
        }
        
        return com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse.builder()
                .lessons(lessonDtos)
                .levels(levelDtos)
                .build();
    }
"""

with open(impl_path, "r", encoding="utf-8") as f:
    impl_content = f.read()

if "getCourseProgress" not in impl_content:
    # Insert before the last bracket
    impl_content = impl_content.rsplit("}", 1)[0] + impl_code + "}\n"
    with open(impl_path, "w", encoding="utf-8") as f:
        f.write(impl_content)


# 3. Update Controller
controller_code = """
    @GetMapping("/progress")
    @Operation(summary = "Lấy toàn bộ tiến độ các bài và level của học viên")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse>> getCourseProgress(
            @org.springframework.security.core.annotation.AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User currentUser) {
        
        com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse data = 
                learnerProgressService.getCourseProgress(currentUser.getId());
        
        return ResponseEntity.ok(ApiResponse.<com.example.vietstage_web_be.dto.response.LearnerCourseProgressResponse>builder()
                .message("Lấy tiến độ học viên thành công")
                .data(data)
                .build());
    }
"""
with open(controller_path, "r", encoding="utf-8") as f:
    controller_content = f.read()

if "getCourseProgress" not in controller_content:
    controller_content = controller_content.rsplit("}", 1)[0] + controller_code + "}\n"
    with open(controller_path, "w", encoding="utf-8") as f:
        f.write(controller_content)

print("Java files updated successfully.")
