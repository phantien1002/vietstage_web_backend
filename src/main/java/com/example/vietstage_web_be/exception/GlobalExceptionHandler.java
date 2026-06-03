package com.example.vietstage_web_be.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý AppException — trả về HTTP status tương ứng với ErrorCode.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse body = ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    /**
     * Xử lý các lỗi không lường trước — trả về 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse body = ErrorResponse.builder()
                .errorCode(9999)
                .message("An unexpected error occurred")
                .build();
        return ResponseEntity.internalServerError().body(body);
    }
}
