package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.DashboardStatsResponse;
import com.example.vietstage_web_be.repository.DashboardRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.service.IAdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final DashboardRepository dashboardRepository;

    @Override
    public DashboardStatsResponse getDashboardStats(LocalDateTime fromDate, LocalDateTime toDate, String granularity) {
        if (fromDate == null) fromDate = LocalDateTime.now().minusDays(30);
        if (toDate == null) toDate = LocalDateTime.now();
        if (granularity == null || granularity.isEmpty()) granularity = "MONTH";

        // 1. Active Users (using aggregate query)
        long activeUsersCount = dashboardRepository.getActiveUsers(fromDate, toDate);

        // 2. Popular Instruments (using aggregate query)
        List<Map<String, Object>> popularInstrumentsRaw = dashboardRepository.getPopularInstruments(fromDate, toDate);
        List<DashboardStatsResponse.PopularInstrument> popularInstruments = popularInstrumentsRaw.stream()
                .map(row -> DashboardStatsResponse.PopularInstrument.builder()
                        .instrumentId(((Number) row.get("id")).longValue())
                        .instrumentName((String) row.get("name"))
                        .practiceCount(((Number) row.get("practice_count")).longValue())
                        .build())
                .collect(Collectors.toList());

        // 3. Session Duration (using aggregate query)
        List<Map<String, Object>> durationRaw = dashboardRepository.getSessionDuration(fromDate, toDate, granularity);
        List<DashboardStatsResponse.SessionDurationData> sessionDuration = durationRaw.stream()
                .map(row -> DashboardStatsResponse.SessionDurationData.builder()
                        .period((String) row.get("period"))
                        .averageDurationMinutes(row.get("average_duration") != null ? ((Number) row.get("average_duration")).doubleValue() : 0.0)
                        .totalDurationMinutes(row.get("total_duration") != null ? ((Number) row.get("total_duration")).doubleValue() : 0.0)
                        .build())
                .collect(Collectors.toList());

        // 4. Retention (using aggregate query)
        List<Map<String, Object>> retentionRaw = dashboardRepository.getRetentionRate(fromDate, toDate, granularity);
        List<DashboardStatsResponse.RetentionData> retention = retentionRaw.stream()
                .map(row -> {
                    long total = ((Number) row.get("total_users")).longValue();
                    long retained = ((Number) row.get("retained_count")).longValue();
                    double rate = total == 0 ? 0 : ((double) retained / total) * 100.0;
                    return DashboardStatsResponse.RetentionData.builder()
                            .period((String) row.get("period"))
                            .retentionRate(rate)
                            .build();
                })
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .activeUsers(activeUsersCount)
                .popularInstruments(popularInstruments)
                .sessionDuration(sessionDuration)
                .retention(retention)
                .build();
    }
}
