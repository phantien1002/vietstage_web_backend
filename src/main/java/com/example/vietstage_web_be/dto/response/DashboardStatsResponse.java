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
        private Long practiceCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionDurationData {
        private String period;
        private Double averageDurationMinutes;
        private Double totalDurationMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetentionData {
        private String period;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Tỷ lệ giữ chân người dùng (%) (Thang 0-100)")
        private Double retentionRate;
    }
}
