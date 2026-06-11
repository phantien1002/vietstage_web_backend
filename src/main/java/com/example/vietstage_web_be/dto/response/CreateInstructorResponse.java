package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CreateInstructorResponse {
    private Long userId;
    private String email;
    private String role;
    private String fullName;
    private String specialization;
    private Long yearsExperience;
    private LocalDateTime createdAt;
}
