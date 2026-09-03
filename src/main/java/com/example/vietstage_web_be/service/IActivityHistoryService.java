package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.ActivityHistoryDetailResponse;
import com.example.vietstage_web_be.dto.response.ActivityHistoryItemResponse;
import com.example.vietstage_web_be.entity.User;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface IActivityHistoryService {
    Page<ActivityHistoryItemResponse> getHistory(User learner, int page, int size, String type, LocalDateTime from, LocalDateTime to);
    ActivityHistoryDetailResponse getDetail(User learner, String eventId);
}
