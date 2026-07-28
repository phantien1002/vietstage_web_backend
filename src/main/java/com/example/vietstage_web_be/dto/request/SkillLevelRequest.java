package com.example.vietstage_web_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SkillLevelRequest {

    @NotBlank(message = "Mã trình độ không được để trống")
    @Size(max = 20, message = "Mã trình độ không được vượt quá 20 ký tự")
    private String levelCode;

    @NotBlank(message = "Tên trình độ không được để trống")
    @Size(max = 50, message = "Tên trình độ không được vượt quá 50 ký tự")
    private String levelName;

    @NotNull(message = "Thứ tự không được để trống")
    private Short orderIndex;
}
