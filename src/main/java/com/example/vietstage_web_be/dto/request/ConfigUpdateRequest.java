package com.example.vietstage_web_be.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdateRequest {
    
    @Schema(description = "Giá trị mới của cấu hình", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Giá trị cấu hình không được để trống")
    private String value;
    
    @Schema(description = "Phiên bản cấu hình để xử lý cập nhật đồng thời (Optimistic Locking)", requiredMode = Schema.RequiredMode.REQUIRED)
    @jakarta.validation.constraints.NotNull(message = "Version không được để trống")
    private Long version;
}
