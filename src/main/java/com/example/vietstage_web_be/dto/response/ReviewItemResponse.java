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
public class ReviewItemResponse {
    private Long id;
    private String title;
    private String instrument;
    private String instructor;
    private String date;
    private List<AssetResponse> assets;
    private String technicalNotes;
    private String description;
    private String status; // 'pending' | 'approved' | 'rejected'
    private String feedback;
    private String approvedBy;
    private String approvedAt;
}
