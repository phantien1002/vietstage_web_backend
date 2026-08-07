package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.DashboardStatsResponse;

import java.time.LocalDateTime;

public interface IAdminDashboardService {
    DashboardStatsResponse getDashboardStats(LocalDateTime fromDate, LocalDateTime toDate, String granularity);
}
