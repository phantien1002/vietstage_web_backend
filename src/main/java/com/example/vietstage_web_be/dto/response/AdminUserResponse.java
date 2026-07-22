package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;


import java.util.List;

@Data
@Builder
public class AdminUserResponse {
    private String id;
    private String name;
    private String email;
    private String role;
    private String registeredAt;
    private String status;
    private String avatar;
    private String initials;
    
    // Admin & Instructor specific fields
    private String specialty;
    private UserStatsDto stats;
    
    // Instructor fields
    private List<String> instruments;
    
    // Recent activities (Audit Trail)
    private List<ActivityDto> activities;
}
