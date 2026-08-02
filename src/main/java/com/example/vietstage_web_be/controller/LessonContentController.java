package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.LessonContentRequest;
import com.example.vietstage_web_be.dto.response.LessonContentResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.ILessonContentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons/{lessonId}/contents")
@RequiredArgsConstructor
@Tag(name = "Lesson Contents", description = "Các API quản lý nội dung của bài học")
public class LessonContentController {

    private final ILessonContentService contentService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<LessonContentResponse>>> getContents(@PathVariable Long lessonId) {
        return ResponseEntity.ok(BaseResponse.success(contentService.getLessonContents(lessonId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<BaseResponse<LessonContentResponse>> addContent(
            @AuthenticationPrincipal User instructor,
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonContentRequest request) {
        LessonContentResponse response = contentService.addContent(instructor, lessonId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PutMapping("/{contentId}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<BaseResponse<LessonContentResponse>> updateContent(
            @AuthenticationPrincipal User instructor,
            @PathVariable Long lessonId,
            @PathVariable Long contentId,
            @Valid @RequestBody LessonContentRequest request) {
        LessonContentResponse response = contentService.updateContent(instructor, lessonId, contentId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/{contentId}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Void> deleteContent(
            @AuthenticationPrincipal User instructor,
            @PathVariable Long lessonId,
            @PathVariable Long contentId) {
        contentService.deleteContent(instructor, lessonId, contentId);
        return ResponseEntity.noContent().build();
    }
}