package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInstructorRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private String specialization;

    @NotBlank
    private Long yearsExperience;

    @NotBlank
    private String fullName;
}
