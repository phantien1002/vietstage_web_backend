package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.LessonTechniqueRequest;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.ILessonTechniqueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons/{lessonId}/techniques")
@RequiredArgsConstructor
@Tag(name = "Lesson Techniques", description = "Các API quản lý kỹ thuật của bài học")
public class LessonTechniqueController {

    private final ILessonTechniqueService techniqueService;

    @PostMapping
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<BaseResponse<String>> addTechnique(
            @AuthenticationPrincipal(expression = "user") User instructor,
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonTechniqueRequest request) {
        techniqueService.addTechnique(instructor, lessonId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Thêm kỹ thuật vào bài học thành công"));
    }

    @DeleteMapping("/{techniqueId}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Void> removeTechnique(
            @AuthenticationPrincipal(expression = "user") User instructor,
            @PathVariable Long lessonId,
            @PathVariable Long techniqueId) {
        techniqueService.removeTechnique(instructor, lessonId, techniqueId);
        return ResponseEntity.noContent().build();
    }
}
