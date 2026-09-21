package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.LessonResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.service.ILessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/app/courses")
@RequiredArgsConstructor
@Tag(name = "App Course", description = "API dành riêng cho VietStageApp để lấy dữ liệu bài học (Public)")
public class AppCourseController {

    private final ILessonService lessonService;
    private final com.example.vietstage_web_be.service.ILearnerProgressService learnerProgressService;

    @GetMapping("/lessons")
    @Operation(summary = "Lấy danh sách các bài học đã được duyệt (App)")
    public ResponseEntity<ApiResponse<PageResponse<LessonResponse>>> getApprovedLessons(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long instrumentId,
            @RequestParam(required = false) Long skillLevelId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResponse<LessonResponse> data = lessonService.getLessons(
                search, instrumentId, skillLevelId, "APPROVED", true, page, size);
                
        return ResponseEntity.ok(ApiResponse.<PageResponse<LessonResponse>>builder()
                .message("Get approved lessons successfully")
                .data(data)
                .build());
    }

    @GetMapping("/lessons/{id}/bundle")
    @Operation(summary = "Lấy toàn bộ bundle của một bài học (Content, Exercise, Assets) cho App")
    public ResponseEntity<ApiResponse<LessonResponse>> getLessonBundle(
            @PathVariable Long id) {
        
        // TODO: Chúng ta có thể tạo thêm getLessonBundle() trong LessonService để trả về đầy đủ
        // content (LessonContent) và cấu hình cụ thể hơn nếu LessonResponse hiện tại chưa đủ.
        LessonResponse data = lessonService.getLessonById(id);
        
        // Cần đảm bảo bài học đã được APPROVED hoặc trạng thái hiển thị VISIBLE
        // Nhưng tạm thời cứ dùng getLessonById
        
        return ResponseEntity.ok(ApiResponse.<LessonResponse>builder()
                .message("Get lesson bundle successfully")
                .data(data)
                .build());
    }

    @GetMapping("/lessons/{id}/access")
    @Operation(summary = "Lấy trạng thái truy cập và tiến độ của bài học cho User hiện tại")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<com.example.vietstage_web_be.dto.response.LessonAccessResponse>> getLessonAccess(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User currentUser) {
        
        com.example.vietstage_web_be.dto.response.LessonAccessResponse data = 
                learnerProgressService.getLessonAccess(currentUser.getId(), id);
        
        return ResponseEntity.ok(ApiResponse.<com.example.vietstage_web_be.dto.response.LessonAccessResponse>builder()
                .message("Lấy trạng thái bài học thành công")
                .data(data)
                .build());
    }

    @PostMapping("/lessons/{id}/start")
    @Operation(summary = "Ghi nhận bắt đầu bài học (chuyển sang IN_PROGRESS)")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ApiResponse<com.example.vietstage_web_be.dto.response.LessonAccessResponse>> startLesson(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal(expression = "user") com.example.vietstage_web_be.entity.User currentUser) {
        
        com.example.vietstage_web_be.dto.response.LessonAccessResponse data = 
                learnerProgressService.startLesson(currentUser.getId(), id);
        
        return ResponseEntity.ok(ApiResponse.<com.example.vietstage_web_be.dto.response.LessonAccessResponse>builder()
                .message("Bắt đầu bài học thành công")
                .data(data)
                .build());
    }
}
