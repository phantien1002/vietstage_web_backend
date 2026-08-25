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
public class CreateCosmeticRequest {

    @NotBlank(message = "Tên vật phẩm không được để trống")
    private String name;

    @Builder.Default
    private String itemType = "ROOM_DECOR";

    private String assetUrl;

    @Builder.Default
    private String unlockType = "STARS";

    @Builder.Default
    private Integer unlockValue = 0;

    @Builder.Default
    private String status = "ACTIVE";
}
