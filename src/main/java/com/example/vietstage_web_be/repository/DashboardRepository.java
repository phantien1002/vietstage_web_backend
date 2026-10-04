package com.example.vietstage_web_be.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class DashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public DashboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long getActiveUsers(LocalDateTime fromDate, LocalDateTime toDate) {
        try {
            String sql = """
                SELECT COUNT(DISTINCT u.user_id)
                FROM users u
                JOIN roles r ON u.role_id = r.role_id
                WHERE r.role_name = 'LEARNER' 
                AND u.user_id IN (
                    SELECT learner_id FROM practice_attempts WHERE completed_at >= ? AND completed_at <= ?
                    UNION
                    SELECT user_id FROM usage_sessions WHERE started_at >= ? AND started_at <= ?
                )
            """;
            Long count = jdbcTemplate.queryForObject(sql, Long.class, fromDate, toDate, fromDate, toDate);
            return count != null ? count : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public List<Map<String, Object>> getPopularInstruments(LocalDateTime fromDate, LocalDateTime toDate) {
        try {
            String sql = """
                SELECT i.id, i.name, COUNT(pa.id) as practice_count
                FROM practice_attempts pa
                JOIN exercises e ON pa.exercise_id = e.id
                JOIN lessons l ON e.lesson_id = l.lesson_id
                JOIN instruments i ON l.instrument_id = i.id
                WHERE pa.completed_at >= ? AND pa.completed_at <= ?
                GROUP BY i.id, i.name
                ORDER BY practice_count DESC
                LIMIT 5
            """;
            return jdbcTemplate.queryForList(sql, fromDate, toDate);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getSessionDuration(LocalDateTime fromDate, LocalDateTime toDate, String granularity) {
        try {
            String dateFormat = getDateFormatForGranularity(granularity);
            String sql = String.format("""
                SELECT TO_CHAR(started_at, '%s') as period,
                       AVG(EXTRACT(EPOCH FROM (ended_at - started_at)) / 60.0) as average_duration,
                       SUM(EXTRACT(EPOCH FROM (ended_at - started_at)) / 60.0) as total_duration
                FROM usage_sessions
                WHERE started_at >= ? AND started_at <= ?
                  AND ended_at IS NOT NULL
                  AND ended_at > started_at
                  AND EXTRACT(EPOCH FROM (ended_at - started_at)) <= 86400
                GROUP BY TO_CHAR(started_at, '%s')
                ORDER BY period ASC
            """, dateFormat, dateFormat);
            return jdbcTemplate.queryForList(sql, fromDate, toDate);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getRetentionRate(LocalDateTime fromDate, LocalDateTime toDate, String granularity) {
        try {
            String dateFormat = getDateFormatForGranularity(granularity);
            String intervalValue = getIntervalForGranularity(granularity);
            String trunc = getTruncGranularity(granularity);
            
            String sql = String.format("""
                WITH periods AS (
                    SELECT DISTINCT user_id, 
                           TO_CHAR(started_at, '%s') as period_str,
                           DATE_TRUNC('%s', started_at) as period_start
                    FROM usage_sessions
                    WHERE started_at >= (?::timestamp - INTERVAL '%s') AND started_at <= ?
                ),
                period_users AS (
                    SELECT period_str, period_start, COUNT(DISTINCT user_id) as total_users
                    FROM periods
                    GROUP BY period_str, period_start
                ),
                retained_users AS (
                    SELECT p1.period_str, COUNT(DISTINCT p1.user_id) as retained_count
                    FROM periods p1
                    JOIN periods p0 ON p1.user_id = p0.user_id 
                    WHERE p0.period_start = p1.period_start - INTERVAL '%s'
                    GROUP BY p1.period_str
                )
                SELECT pu.period_str as period,
                       pu.total_users,
                       COALESCE(ru.retained_count, 0) as retained_count
                FROM period_users pu
                LEFT JOIN retained_users ru ON pu.period_str = ru.period_str
                WHERE pu.period_start >= DATE_TRUNC('%s', ?::timestamp)
                ORDER BY pu.period_str ASC
            """, dateFormat, trunc, intervalValue, intervalValue, trunc);
            return jdbcTemplate.queryForList(sql, fromDate, toDate, fromDate);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    private String getDateFormatForGranularity(String granularity) {
        if ("DAY".equalsIgnoreCase(granularity)) return "YYYY-MM-DD";
        if ("WEEK".equalsIgnoreCase(granularity)) return "IYYY-IW";
        return "YYYY-MM"; // MONTH
    }

    private String getTruncGranularity(String granularity) {
        if ("DAY".equalsIgnoreCase(granularity)) return "day";
        if ("WEEK".equalsIgnoreCase(granularity)) return "week";
        return "month"; 
    }

    private String getIntervalForGranularity(String granularity) {
        if ("DAY".equalsIgnoreCase(granularity)) return "1 day";
        if ("WEEK".equalsIgnoreCase(granularity)) return "1 week";
        return "1 month";
    }
}
