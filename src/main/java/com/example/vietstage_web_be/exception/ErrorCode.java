package com.example.vietstage_web_be.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(1001, HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXIST(1002, HttpStatus.CONFLICT),
    EMAIL_NOT_FOUND(1003, HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1004, HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(1005, HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_MATCH(1006, HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1007, HttpStatus.NOT_FOUND),
    INVALID_ROLE(1008, HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(1009, HttpStatus.FORBIDDEN),
    INVALID_VERIFICATION_CODE(1010, HttpStatus.BAD_REQUEST);

    private final int code;
    private final HttpStatus httpStatus;

    ErrorCode(int code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
