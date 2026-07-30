package com.example.vietstage_web_be.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkPracticeAttemptRequest {
    @NotEmpty(message = "Danh sách attempts không được rỗng")
    @Valid
    private List<PracticeAttemptRequest> attempts;
}