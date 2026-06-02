package com.example.vietstage_web_be.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String password;
    private String email;
    private String fullName;
}
