package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.UsageSessionRequest;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IUsageSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/usage-sessions")
@RequiredArgsConstructor
@Tag(name = "Usage Session", description = "Telemetry cho session người dùng")
public class UsageSessionController {

    private final IUsageSessionService usageSessionService;

    @PostMapping("/start")
    public ResponseEntity<BaseResponse<Map<String, UUID>>> startSession(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestBody(required = false) UsageSessionRequest request) {
        String platform = (request != null && request.getPlatform() != null) ? request.getPlatform() : "WEB";
        UUID sessionId = usageSessionService.startSession(user, platform);
        return ResponseEntity.ok(BaseResponse.success(Map.of("sessionId", sessionId)));
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<BaseResponse<String>> endSession(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable UUID sessionId) {
        usageSessionService.endSession(user, sessionId);
        return ResponseEntity.ok(BaseResponse.success("Session ended successfully"));
    }
}
