package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Loại vật phẩm không được để trống")
    @Pattern(regexp = "^(ROOM_DECOR|AVATAR|INSTRUMENT_SKIN)$", message = "Loại vật phẩm không hợp lệ")
    @Builder.Default
    private String itemType = "ROOM_DECOR";

    @NotBlank(message = "URL hình ảnh không được để trống")
    @Pattern(regexp = "^(http|https)://.*$", message = "URL hình ảnh phải là một link hợp lệ")
    private String assetUrl;

    @jakarta.validation.constraints.NotNull(message = "Giá sao không được để trống")
    @jakarta.validation.constraints.Min(value = 0, message = "Giá trị mở khóa phải lớn hơn hoặc bằng 0")
    @io.swagger.v3.oas.annotations.media.Schema(requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private Integer starPrice = 0;

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Trạng thái phải là ACTIVE hoặc INACTIVE")
    @Builder.Default
    private String status = "ACTIVE";
}
