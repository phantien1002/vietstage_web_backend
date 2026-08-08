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
        String sql = """
            SELECT COUNT(DISTINCT user_id) FROM (
                SELECT learner_id as user_id FROM practice_attempts WHERE created_at >= ? AND created_at <= ?
                UNION
                SELECT user_id FROM usage_sessions WHERE started_at >= ? AND started_at <= ?
            ) AS combined_users
        """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, fromDate, toDate, fromDate, toDate);
        return count != null ? count : 0L;
    }

    public List<Map<String, Object>> getPopularInstruments(LocalDateTime fromDate, LocalDateTime toDate) {
        String sql = """
            SELECT i.id, i.name, COUNT(pa.id) as practice_count
            FROM practice_attempts pa
            JOIN exercises e ON pa.exercise_id = e.id
            JOIN lessons l ON e.lesson_id = l.lesson_id
            JOIN instruments i ON l.instrument_id = i.id
            WHERE pa.created_at >= ? AND pa.created_at <= ?
            GROUP BY i.id, i.name
            ORDER BY practice_count DESC
            LIMIT 5
        """;
        return jdbcTemplate.queryForList(sql, fromDate, toDate);
    }

    public List<Map<String, Object>> getSessionDuration(LocalDateTime fromDate, LocalDateTime toDate, String granularity) {
        String dateFormat = getDateFormatForGranularity(granularity);
        String sql = String.format("""
            SELECT TO_CHAR(started_at, '%s') as period,
                   AVG(duration_minutes) as average_duration,
                   SUM(duration_minutes) as total_duration
            FROM practice_sessions
            WHERE started_at >= ? AND started_at <= ?
            GROUP BY TO_CHAR(started_at, '%s')
            ORDER BY period ASC
        """, dateFormat, dateFormat);
        return jdbcTemplate.queryForList(sql, fromDate, toDate);
    }

    public List<Map<String, Object>> getRetentionRate(LocalDateTime fromDate, LocalDateTime toDate, String granularity) {
        String dateFormat = getDateFormatForGranularity(granularity);
        // Calculate retention: users active in this period who were also active in the PREVIOUS period.
        // For simplicity and efficiency, we compare user activity across periods.
        String sql = String.format("""
            WITH periods AS (
                SELECT DISTINCT learner_id as user_id, TO_CHAR(started_at, '%s') as period
                FROM practice_sessions
                WHERE started_at >= ? AND started_at <= ?
                UNION
                SELECT DISTINCT user_id, TO_CHAR(started_at, '%s') as period
                FROM usage_sessions
                WHERE started_at >= ? AND started_at <= ?
            ),
            period_users AS (
                SELECT period, COUNT(DISTINCT user_id) as total_users
                FROM periods
                GROUP BY period
            ),
            retained_users AS (
                SELECT p1.period, COUNT(DISTINCT p1.user_id) as retained_count
                FROM periods p1
                JOIN periods p0 ON p1.user_id = p0.user_id AND p1.period > p0.period
                -- simplified: just checking if they existed in any previous period within the range
                GROUP BY p1.period
            )
            SELECT pu.period,
                   pu.total_users,
                   COALESCE(ru.retained_count, 0) as retained_count
            FROM period_users pu
            LEFT JOIN retained_users ru ON pu.period = ru.period
            ORDER BY pu.period ASC
        """, dateFormat, dateFormat);
        return jdbcTemplate.queryForList(sql, fromDate, toDate, fromDate, toDate);
    }

    private String getDateFormatForGranularity(String granularity) {
        if ("DAY".equalsIgnoreCase(granularity)) {
            return "YYYY-MM-DD";
        } else if ("WEEK".equalsIgnoreCase(granularity)) {
            return "IYYY-IW";
        } else {
            return "YYYY-MM"; // MONTH
        }
    }
}
