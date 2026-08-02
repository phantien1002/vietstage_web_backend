package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Mật khẩu cũ không được để trống")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, message = "Mật khẩu mới phải có ít nhất 8 ký tự")
    @Pattern(regexp = "^\\S*$", message = "Mật khẩu không được chứa khoảng trắng")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    @Pattern(regexp = "^\\S*$", message = "Mật khẩu không được chứa khoảng trắng")
    private String confirmPassword;
}
