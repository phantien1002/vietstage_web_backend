package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdateRequest {
    
    @NotBlank(message = "Giá trị cấu hình không được để trống")
    private String value;
    
    private Long version;
}
