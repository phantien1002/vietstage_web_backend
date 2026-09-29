package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    @io.swagger.v3.oas.annotations.media.Schema(description = "Số learner khác nhau có hoạt động học (từ practice_attempts hoặc practice_sessions) trong khoảng thời gian; không tính admin/instructor")
    private long activeUsers; 
    
    private List<PopularInstrument> popularInstruments; // new
    private List<SessionDurationData> sessionDuration; // new
    private List<RetentionData> retention; // new

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularInstrument {
        private Long instrumentId;
        private String instrumentName;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Số lượt luyện tập hợp lệ theo chuỗi attempt -> exercise -> lesson -> instrument")
        private Long practiceCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionDurationData {
        private String period;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Thời lượng trung bình mỗi phiên (phút). Tính từ (endedAt - startedAt) của các phiên đã đóng.")
        private Double averageDurationMinutes;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Tổng thời lượng các phiên (phút). Tính từ (endedAt - startedAt) của các phiên đã đóng.")
        private Double totalDurationMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetentionData {
        private String period;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Tỷ lệ giữ chân người dùng (%). Tính bằng: (Learner hoạt động ở kỳ N-1 và quay lại ở kỳ N) / (Learner hoạt động ở kỳ N-1) * 100")
        private Double retentionRate;
    }
}
