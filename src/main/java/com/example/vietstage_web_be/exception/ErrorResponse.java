package com.example.vietstage_web_be.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private int errorCode;
    private String message;
}
