import os

controller_path = 'src/main/java/com/example/vietstage_web_be/controller/LessonController.java'
with open(controller_path, 'r', encoding='utf-8') as f:
    content = f.read()

new_endpoint = """
    @GetMapping("/{lessonCode}/revisions/{revision}/config")
    @Operation(summary = "L?y c?u h?nh b?i h?c theo phi?n b?n (PUBLIC)")
    public ResponseEntity<ApiResponse<com.example.vietstage_web_be.dto.response.LessonConfigResponse>> getLessonConfig(
            @PathVariable String lessonCode,
            @PathVariable Integer revision) {
        
        com.example.vietstage_web_be.dto.response.LessonConfigResponse data = lessonService.getLessonConfig(lessonCode, revision);
        return ResponseEntity.ok(ApiResponse.<com.example.vietstage_web_be.dto.response.LessonConfigResponse>builder()
                .message("Get lesson config successfully")
                .data(data)
                .build());
    }
"""

if 'getLessonConfig' not in content:
    content = content.replace('}', new_endpoint + '\n}')
    with open(controller_path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Added getLessonConfig to LessonController")
