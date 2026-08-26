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
public class UpdateCosmeticRequest {

    @NotBlank(message = "Tên vật phẩm không được để trống")
    private String name;

    @Pattern(regexp = "^(ROOM_DECOR|AVATAR|INSTRUMENT_SKIN)$", message = "Loại vật phẩm không hợp lệ")
    private String itemType;

    @Pattern(regexp = "^(http|https)://.*$", message = "URL hình ảnh phải là một link hợp lệ")
    private String assetUrl;

    @Pattern(regexp = "^(STARS|ACHIEVEMENT|DEFAULT)$", message = "Loại mở khóa không hợp lệ")
    private String unlockType;

    @jakarta.validation.constraints.Min(value = 0, message = "Giá trị mở khóa phải lớn hơn hoặc bằng 0")
    private Integer unlockValue;

    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Trạng thái phải là ACTIVE hoặc INACTIVE")
    private String status;
}
