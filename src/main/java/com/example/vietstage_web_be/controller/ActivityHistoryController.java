package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.response.ActivityHistoryDetailResponse;
import com.example.vietstage_web_be.dto.response.ActivityHistoryItemResponse;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.service.IActivityHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users/me/activity-history")
@RequiredArgsConstructor
public class ActivityHistoryController {
    private final IActivityHistoryService activityHistoryService;

    @GetMapping
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<Page<ActivityHistoryItemResponse>>> getHistory(
            @AuthenticationPrincipal(expression = "user") User learner,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(BaseResponse.success(activityHistoryService.getHistory(learner, page, size, type, from, to)));
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("hasAuthority('LEARNER')")
    public ResponseEntity<BaseResponse<ActivityHistoryDetailResponse>> getDetail(
            @AuthenticationPrincipal(expression = "user") User learner,
            @PathVariable String eventId) {
        return ResponseEntity.ok(BaseResponse.success(activityHistoryService.getDetail(learner, eventId)));
    }
}
