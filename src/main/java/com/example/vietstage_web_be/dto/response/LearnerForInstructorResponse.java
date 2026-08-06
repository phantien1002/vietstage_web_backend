package com.example.vietstage_web_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerForInstructorResponse {
    private Long id;
    private String fullName;
    private String email;
    private String userCode;
    private String instrumentName;
}
