package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.ConfigUpdateRequest;
import com.example.vietstage_web_be.dto.response.AppConfigResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IAppConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "App Configs", description = "Các API cấu hình hệ thống")
public class AppConfigController {

    private final IAppConfigService appConfigService;

    @GetMapping("/admin/configs")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Lấy tất cả cấu hình hệ thống (Dành cho Admin)")
    public ResponseEntity<BaseResponse<List<AppConfigResponse>>> getAllConfigs(
            @RequestParam(required = false) String group) {
        
        List<AppConfigResponse> responses = appConfigService.getAllConfigs(group);
        return ResponseEntity.ok(BaseResponse.success(responses));
    }

    @PutMapping("/admin/configs/{key}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Cập nhật một cấu hình")
    public ResponseEntity<BaseResponse<AppConfigResponse>> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody ConfigUpdateRequest request,
            @AuthenticationPrincipal(expression = "user") User user) {
        
        AppConfigResponse response = appConfigService.updateConfig(key, request, user);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/configs")
    @Operation(summary = "Lấy cấu hình công khai (Client game engine dùng)")
    public ResponseEntity<BaseResponse<Map<String, String>>> getPublicConfigs(
            @RequestParam(required = false, defaultValue = "scoring") String group) {
        
        Map<String, String> configs = appConfigService.getPublicConfigs(group);
        return ResponseEntity.ok(BaseResponse.success(configs));
    }
}