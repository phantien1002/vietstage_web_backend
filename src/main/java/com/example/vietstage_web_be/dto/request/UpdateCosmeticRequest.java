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
public class UpdateCosmeticRequest {

    @NotBlank(message = "Tên vật phẩm không được để trống")
    private String name;

    private String itemType;

    private String assetUrl;

    private String unlockType;

    private Integer unlockValue;

    private String status;
}
