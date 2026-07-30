package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkPracticeAttemptResponse {
    private int created;
    private List<Conflict> conflicts;

    @Data
    @Builder
    public static class Conflict {
        private String clientUuid;
        private String reason;
    }
}