package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Verification code cannot be blank")
    private String verificationCode;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @Pattern(regexp = "^\\S*$", message = "Password cannot contain spaces")
    private String newPassword;
}
