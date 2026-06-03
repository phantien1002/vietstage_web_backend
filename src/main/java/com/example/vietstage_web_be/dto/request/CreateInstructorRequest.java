package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInstructorRequest {
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Specialization cannot be blank")
    private String specialization;

    @NotBlank(message = "Number of experience years cannot be blank")
    private Long yearsExperience;

    @NotBlank(message = "Name of instructor cannot be blank")
    private String fullName;
}
