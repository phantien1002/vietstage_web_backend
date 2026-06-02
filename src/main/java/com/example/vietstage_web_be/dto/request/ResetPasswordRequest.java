package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Verification code cannot be blank")
    private String verificationCode;

    private String newPassword;
}
