package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdateRequest {
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(?i)(LEARNER|INSTRUCTOR)$", message = "Role must be LEARNER or INSTRUCTOR")
    @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"LEARNER", "INSTRUCTOR"})
    private String role;
}
