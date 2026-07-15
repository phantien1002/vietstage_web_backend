package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InstructorCreateResponse {
    private Long id;
    private String email;
    private String fullName;
    private String roleName;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Thông tin Profile đi kèm
    private String specialization;
    private String biography;
    private Long yearsExperience;
}
