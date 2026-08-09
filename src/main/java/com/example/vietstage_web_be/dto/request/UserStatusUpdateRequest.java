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
public class UserStatusUpdateRequest {
    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "^(?i)(ACTIVE|LOCKED)$", message = "Trạng thái phải là ACTIVE hoặc LOCKED")
    private String status;
}
