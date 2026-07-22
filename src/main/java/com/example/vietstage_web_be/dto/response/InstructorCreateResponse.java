package com.example.vietstage_web_be.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
public class InstructorCreateResponse {
    private Long id;
    private String userCode;

    private String email;
    private String fullName;
    private String roleName;
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // Thông tin Profile đi kèm
    private String biography;
    private Integer yearsExperience;
}
