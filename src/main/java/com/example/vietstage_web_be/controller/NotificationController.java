package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.response.NotificationListResponse;
import com.example.vietstage_web_be.dto.response.NotificationResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Các API thông báo")
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy danh sách thông báo")
    public ResponseEntity<BaseResponse<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        NotificationListResponse response = notificationService.getNotifications(user, isRead, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu 1 thông báo đã đọc")
    public ResponseEntity<BaseResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "user") User user) {
        
        NotificationResponse response = notificationService.markAsRead(id, user);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu tất cả đã đọc")
    public ResponseEntity<BaseResponse<Map<String, Integer>>> markAllAsRead(
            @AuthenticationPrincipal(expression = "user") User user) {
        
        int updatedCount = notificationService.markAllAsRead(user);
        return ResponseEntity.ok(BaseResponse.success(Map.of("updated_count", updatedCount)));
    }
}