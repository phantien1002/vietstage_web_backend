package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.DashboardStatsResponse;
import com.example.vietstage_web_be.entity.PracticeSession;
import com.example.vietstage_web_be.entity.PracticeAttempt;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.PracticeSessionRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.service.IAdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final PracticeSessionRepository practiceSessionRepository;

    @Override
    public DashboardStatsResponse getDashboardStats(LocalDateTime fromDate, LocalDateTime toDate, String granularity) {
        long totalUsers = userRepository.count();
        long totalLessons = lessonRepository.count();

        long activeInstructors = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "INSTRUCTOR".equalsIgnoreCase(u.getRole().getName()) && Boolean.TRUE.equals(u.getActive()))
                .count();

        long totalRevenue = 150000000L; // Mock revenue

        List<PracticeSession> sessions = practiceSessionRepository.findAllByDateRange(fromDate, toDate);

        // 1. Active Users
        long activeUsersCount = sessions.stream()
                .map(s -> s.getLearner().getId())
                .distinct()
                .count();

        // 2. Popular Instruments
        Map<Long, String> instrumentNames = new HashMap<>();
        Map<Long, Long> instrumentCounts = new HashMap<>();
        
        for (PracticeSession s : sessions) {
            if (s.getPracticeAttempts() != null) {
                for (PracticeAttempt a : s.getPracticeAttempts()) {
                    if (a.getExercise() != null && a.getExercise().getLesson() != null && a.getExercise().getLesson().getInstrument() != null) {
                        Long instId = a.getExercise().getLesson().getInstrument().getId();
                        String instName = a.getExercise().getLesson().getInstrument().getName();
                        instrumentNames.put(instId, instName);
                        instrumentCounts.put(instId, instrumentCounts.getOrDefault(instId, 0L) + 1);
                    }
                }
            }
        }

        List<DashboardStatsResponse.PopularInstrument> popularInstruments = instrumentCounts.entrySet().stream()
                .map(e -> DashboardStatsResponse.PopularInstrument.builder()
                        .instrumentId(e.getKey())
                        .instrumentName(instrumentNames.get(e.getKey()))
                        .practiceCount(e.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getPracticeCount(), a.getPracticeCount()))
                .limit(5)
                .collect(Collectors.toList());

        // 3. Session Duration
        Map<String, List<PracticeSession>> sessionsByPeriod = groupSessionsByPeriod(sessions, granularity);
        List<DashboardStatsResponse.SessionDurationData> sessionDuration = new ArrayList<>();
        
        for (Map.Entry<String, List<PracticeSession>> entry : sessionsByPeriod.entrySet()) {
            double totalDuration = entry.getValue().stream()
                    .filter(s -> s.getDurationMinutes() != null)
                    .mapToDouble(PracticeSession::getDurationMinutes)
                    .sum();
            double avgDuration = entry.getValue().isEmpty() ? 0 : totalDuration / entry.getValue().size();
            
            sessionDuration.add(DashboardStatsResponse.SessionDurationData.builder()
                    .period(entry.getKey())
                    .averageDurationMinutes(avgDuration)
                    .totalDurationMinutes(totalDuration)
                    .build());
        }
        // sort by period (alphabetical is mostly correct for iso dates, but we can just let frontend handle)
        sessionDuration.sort(Comparator.comparing(DashboardStatsResponse.SessionDurationData::getPeriod));

        // 4. Retention (Mock formula based on active users vs total users, per period)
        List<DashboardStatsResponse.RetentionData> retention = new ArrayList<>();
        for (Map.Entry<String, List<PracticeSession>> entry : sessionsByPeriod.entrySet()) {
            long activeInPeriod = entry.getValue().stream()
                    .map(s -> s.getLearner().getId())
                    .distinct()
                    .count();
            
            double rate = totalUsers == 0 ? 0 : ((double) activeInPeriod / totalUsers) * 100.0;
            retention.add(DashboardStatsResponse.RetentionData.builder()
                    .period(entry.getKey())
                    .retentionRate(rate)
                    .build());
        }
        retention.sort(Comparator.comparing(DashboardStatsResponse.RetentionData::getPeriod));

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsersCount)
                .totalLessons(totalLessons)
                .activeInstructors(activeInstructors)
                .totalRevenue(totalRevenue)
                .popularInstruments(popularInstruments)
                .sessionDuration(sessionDuration)
                .retention(retention)
                .build();
    }

    private Map<String, List<PracticeSession>> groupSessionsByPeriod(List<PracticeSession> sessions, String granularity) {
        Map<String, List<PracticeSession>> grouped = new HashMap<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (PracticeSession s : sessions) {
            String periodKey = "UNKNOWN";
            if (s.getStartedAt() != null) {
                if ("DAY".equalsIgnoreCase(granularity)) {
                    periodKey = s.getStartedAt().format(dayFormatter);
                } else if ("WEEK".equalsIgnoreCase(granularity)) {
                    int week = s.getStartedAt().get(WeekFields.ISO.weekOfWeekBasedYear());
                    int year = s.getStartedAt().getYear();
                    periodKey = year + "-W" + String.format("%02d", week);
                } else { // MONTH or default
                    periodKey = s.getStartedAt().format(monthFormatter);
                }
            }
            grouped.computeIfAbsent(periodKey, k -> new ArrayList<>()).add(s);
        }
        return grouped;
    }
}
