package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.DashboardStatsResponse;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.service.IAdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalLessons = lessonRepository.count();
        
        long activeInstructors = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "INSTRUCTOR".equalsIgnoreCase(u.getRole().getName()) && Boolean.TRUE.equals(u.getActive()))
                .count();

        // Mock revenue for now as it may require complex billing modules
        long totalRevenue = 150000000L; // 150 mil VND

        List<DashboardStatsResponse.ChartData> chartData = List.of(
                new DashboardStatsResponse.ChartData("T1", 4000, 24000000),
                new DashboardStatsResponse.ChartData("T2", 3000, 13980000),
                new DashboardStatsResponse.ChartData("T3", 2000, 98000000),
                new DashboardStatsResponse.ChartData("T4", 2780, 39080000),
                new DashboardStatsResponse.ChartData("T5", 1890, 48000000),
                new DashboardStatsResponse.ChartData("T6", 2390, 38000000)
        );

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalLessons(totalLessons)
                .activeInstructors(activeInstructors)
                .totalRevenue(totalRevenue)
                .chartData(chartData)
                .build();
    }
}
