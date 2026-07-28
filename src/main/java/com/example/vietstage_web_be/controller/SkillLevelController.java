package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.request.SkillLevelRequest;
import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.dto.response.SkillLevelResponse;
import com.example.vietstage_web_be.service.ISkillLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skill-levels")
@RequiredArgsConstructor
@Tag(name = "Skill Levels", description = "Các API quản lý Trình độ bài học")
public class SkillLevelController {

    private final ISkillLevelService skillLevelService;

    @GetMapping
    @Operation(summary = "Lấy tất cả trình độ (PUBLIC)")
    public ResponseEntity<ApiResponse<List<SkillLevelResponse>>> getAllSkillLevels() {
        List<SkillLevelResponse> data = skillLevelService.getAllSkillLevels();
        return ResponseEntity.ok(ApiResponse.<List<SkillLevelResponse>>builder()
                .message("Get all skill levels successfully")
                .data(data)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết trình độ (PUBLIC)")
    public ResponseEntity<ApiResponse<SkillLevelResponse>> getSkillLevelById(@PathVariable Long id) {
        SkillLevelResponse data = skillLevelService.getSkillLevelById(id);
        return ResponseEntity.ok(ApiResponse.<SkillLevelResponse>builder()
                .message("Get skill level successfully")
                .data(data)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm mới trình độ (ADMIN)")
    public ResponseEntity<ApiResponse<SkillLevelResponse>> createSkillLevel(
            @RequestBody @Valid SkillLevelRequest request) {
        SkillLevelResponse data = skillLevelService.createSkillLevel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SkillLevelResponse>builder()
                        .message("Skill level created successfully")
                        .data(data)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật trình độ (ADMIN)")
    public ResponseEntity<ApiResponse<SkillLevelResponse>> updateSkillLevel(
            @PathVariable Long id,
            @RequestBody @Valid SkillLevelRequest request) {
        SkillLevelResponse data = skillLevelService.updateSkillLevel(id, request);
        return ResponseEntity.ok(ApiResponse.<SkillLevelResponse>builder()
                .message("Skill level updated successfully")
                .data(data)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa trình độ (ADMIN)")
    public ResponseEntity<Void> deleteSkillLevel(@PathVariable Long id) {
        skillLevelService.deleteSkillLevel(id);
        return ResponseEntity.noContent().build();
    }
}
