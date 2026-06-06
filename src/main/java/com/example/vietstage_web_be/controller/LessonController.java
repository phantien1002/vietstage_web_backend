package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.LessonRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.LessonResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.service.ILessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "APIs for lesson management and retrieval")
public class LessonController {

    private final ILessonService lessonService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create a new lesson (ADMIN or INSTRUCTOR only)")
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(
            @RequestBody @Valid LessonRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        LessonResponse data = lessonService.createLesson(request, email);

        ApiResponse<LessonResponse> response = ApiResponse.<LessonResponse>builder()
                .message("Lesson created successfully")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get list of lessons with search, filters, pagination, and sorting")
    public ResponseEntity<ApiResponse<PageResponse<LessonResponse>>> getLessons(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long instrumentId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "false") boolean sortDescending) {

        PageResponse<LessonResponse> data = lessonService.getLessons(
                search, instrumentId, difficulty, pageNumber, pageSize, sortBy, sortDescending);

        ApiResponse<PageResponse<LessonResponse>> response = ApiResponse.<PageResponse<LessonResponse>>builder()
                .message("Get lessons successfully")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed lesson by ID")
    public ResponseEntity<ApiResponse<LessonResponse>> getLessonById(@PathVariable Long id) {
        LessonResponse data = lessonService.getLessonById(id);

        ApiResponse<LessonResponse> response = ApiResponse.<LessonResponse>builder()
                .message("Get lesson detail successfully")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update an existing lesson (ADMIN or the creator INSTRUCTOR)")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @PathVariable Long id,
            @RequestBody @Valid LessonRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        LessonResponse data = lessonService.updateLesson(id, request, email);

        ApiResponse<LessonResponse> response = ApiResponse.<LessonResponse>builder()
                .message("Lesson updated successfully")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete a lesson (ADMIN or the creator INSTRUCTOR)")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        lessonService.deleteLesson(id, email);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Lesson deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}
