package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.LessonRequest;
import com.example.vietstage_web_be.dto.request.LessonStatusRequest;
import com.example.vietstage_web_be.dto.request.UpdateLessonRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.LessonResponse;
import com.example.vietstage_web_be.dto.response.LessonStatusResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.service.ILessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "Các API quản lý Bài học")
public class LessonController {

    private final ILessonService lessonService;

    /**
     * GET /api/lessons
     * PUBLIC — danh sách bài học, có thể lọc theo instrument_id, skill_level_id, status.
     * Phân trang theo page & size.
     */
    @GetMapping
    @Operation(summary = "Danh sách bài học (PUBLIC)")
    public ResponseEntity<ApiResponse<PageResponse<LessonResponse>>> getLessons(
            @RequestParam(required = false) String search,
            @RequestParam(value = "instrument_id", required = false) Long instrumentId,
            @RequestParam(value = "skill_level_id", required = false) Long skillLevelId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "false") boolean mine,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        String creatorEmail = mine
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;
        PageResponse<LessonResponse> data = lessonService.getLessons(
                search, instrumentId, skillLevelId, status, creatorEmail, page, size);

        return ResponseEntity.ok(ApiResponse.<PageResponse<LessonResponse>>builder()
                .message("Get lessons successfully")
                .data(data)
                .build());
    }

    /**
     * GET /api/lessons/{id}
     * PUBLIC — chi tiết đầy đủ bài học (contents + assets + exercises + techniques + mini_games).
     */
    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết bài học (PUBLIC)")
    public ResponseEntity<ApiResponse<LessonResponse>> getLessonById(@PathVariable Long id) {
        LessonResponse data = lessonService.getLessonById(id);

        return ResponseEntity.ok(ApiResponse.<LessonResponse>builder()
                .message("Get lesson detail successfully")
                .data(data)
                .build());
    }

    /**
     * POST /api/lessons
     * INSTRUCTOR only — tạo bài học mới. Status mặc định = DRAFT. Trả 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Tạo bài học mới (INSTRUCTOR)")
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(
            @RequestBody @Valid LessonRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        LessonResponse data = lessonService.createLesson(request, email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<LessonResponse>builder()
                        .message("Lesson created successfully")
                        .data(data)
                        .build());
    }

    /**
     * PUT /api/lessons/{id}
     * INSTRUCTOR, ADMIN — cập nhật: title, description, order_index, skill_level_id.
     * instrument_id KHÔNG được thay đổi sau khi tạo.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Cập nhật bài học (INSTRUCTOR, ADMIN)")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @PathVariable Long id,
            @RequestBody @Valid UpdateLessonRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        LessonResponse data = lessonService.updateLesson(id, request, email);

        return ResponseEntity.ok(ApiResponse.<LessonResponse>builder()
                .message("Lesson updated successfully")
                .data(data)
                .build());
    }

    /**
     * PUT /api/lessons/{id}/status
     * INSTRUCTOR: chỉ được đặt PENDING (nộp bài duyệt).
     * ADMIN: được đặt APPROVED hoặc REJECTED (kèm comment nếu REJECTED).
     * Ghi content_reviews + gửi notification cho người tạo bài học.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Đổi trạng thái bài học (Chỉ dành cho INSTRUCTOR chuyển sang PENDING)")
    public ResponseEntity<ApiResponse<LessonStatusResponse>> updateLessonStatus(
            @PathVariable Long id,
            @RequestBody @Valid LessonStatusRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        LessonStatusResponse data = lessonService.updateLessonStatus(id, request, email);

        return ResponseEntity.ok(ApiResponse.<LessonStatusResponse>builder()
                .message("Lesson status updated successfully")
                .data(data)
                .build());
    }

    /**
     * DELETE /api/lessons/{id}
     * INSTRUCTOR, ADMIN — xóa bài học. Trả 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Xóa bài học (INSTRUCTOR, ADMIN)")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        lessonService.deleteLesson(id, email);
        return ResponseEntity.noContent().build();
    }
}
