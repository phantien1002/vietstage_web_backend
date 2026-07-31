package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.response.ApiResponse;
import com.example.vietstage_web_be.service.IUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "API Tải lên file Media")
public class UploadController {

    private final IUploadService uploadService;

    @PostMapping
    @PreAuthorize("hasAnyRole('LEARNER', 'INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Tải file lên Cloudinary", description = "Trả về URL bảo mật của file sau khi tải lên thành công")
    public ResponseEntity<ApiResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = uploadService.uploadFile(file);
        
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("File uploaded successfully")
                .data(url)
                .build());
    }
}
