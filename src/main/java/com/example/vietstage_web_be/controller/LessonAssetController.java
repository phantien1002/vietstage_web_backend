package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.LessonAssetRequest;
import com.example.vietstage_web_be.dto.response.LessonAssetResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.ILessonAssetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lessons/{lessonId}/assets")
@RequiredArgsConstructor
@Tag(name = "Lesson Assets", description = "Các API quản lý file media của bài học")
public class LessonAssetController {

    private final ILessonAssetService assetService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<LessonAssetResponse>>> getAssets(
            @PathVariable Long lessonId,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(BaseResponse.success(assetService.getLessonAssets(lessonId, type)));
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Tải file media cho bài học", description = "Tải lên MP3/WAV hoặc PNG/JPEG/WebP", responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tải lên thành công")
    })
    public ResponseEntity<BaseResponse<LessonAssetResponse>> uploadAsset(
            @AuthenticationPrincipal(expression = "user") User instructor,
            @PathVariable Long lessonId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "tempo_bpm", required = false) Integer tempoBpm,
            @RequestParam(value = "duration_sec", required = false) Integer durationSec) {
        LessonAssetResponse response = assetService.uploadAsset(instructor, lessonId, file, type, tempoBpm, durationSec);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @PutMapping("/{assetId}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<BaseResponse<LessonAssetResponse>> updateAssetMetadata(
            @AuthenticationPrincipal(expression = "user") User instructor,
            @PathVariable Long lessonId,
            @PathVariable Long assetId,
            @Valid @RequestBody LessonAssetRequest request) {
        LessonAssetResponse response = assetService.updateAssetMetadata(instructor, lessonId, assetId, request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @DeleteMapping("/{assetId}")
    @PreAuthorize("hasAnyAuthority('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteAsset(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Long lessonId,
            @PathVariable Long assetId) {
        assetService.deleteAsset(user, lessonId, assetId);
        return ResponseEntity.noContent().build();
    }
}