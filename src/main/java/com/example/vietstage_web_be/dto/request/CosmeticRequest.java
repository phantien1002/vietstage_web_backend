package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CosmeticRequest {

    @NotBlank(message = "Tên vật phẩm không được để trống")
    private String name;

    @NotBlank(message = "Loại vật phẩm không được để trống")
    private String itemType;

    private String assetUrl;

    @NotBlank(message = "Cách mở khóa không được để trống")
    private String unlockType;

    private Integer unlockValue;
}
