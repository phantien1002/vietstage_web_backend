package com.example.vietstage_web_be.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(1001, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXIST(1002, "Email đã tồn tại", HttpStatus.CONFLICT),
    EMAIL_NOT_FOUND(1003, "Không tìm thấy email", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1004, "Mật khẩu không hợp lệ", HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(1005, "Mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_MATCH(1006, "Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1007, "Vai trò không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_ROLE(1008, "Vai trò không hợp lệ", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(1009, "Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),
    INVALID_VERIFICATION_CODE(1010, "Mã xác thực không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    LESSON_NOT_FOUND(2001, "Bài học không tồn tại", HttpStatus.NOT_FOUND),
    INSTRUMENT_NOT_FOUND(2002, "Nhạc cụ không tồn tại", HttpStatus.NOT_FOUND),
    TECHNIQUE_NOT_FOUND(2003, "Kỹ thuật không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_LESSON_ACCESS(2004, "Bạn không có quyền thao tác trên bài học này", HttpStatus.FORBIDDEN),
    INSTRUMENT_ALREADY_EXIST(2005, "Tên nhạc cụ đã tồn tại", HttpStatus.CONFLICT),
    TECHNIQUE_ALREADY_EXIST(2006, "Kỹ thuật đã tồn tại trong nhạc cụ này", HttpStatus.CONFLICT),
    LESSON_ALREADY_EXIST(2007, "Tiêu đề bài học đã tồn tại trong nhạc cụ này", HttpStatus.CONFLICT),
    INVALID_LESSON_STATUS(2008, "Trạng thái bài học không hợp lệ", HttpStatus.BAD_REQUEST),
    LESSON_STATUS_FORBIDDEN(2009, "Chỉ ADMIN mới có quyền duyệt/từ chối bài học", HttpStatus.FORBIDDEN),
    INSTRUCTOR_FORBIDDEN(2010, "Chỉ INSTRUCTOR chỉ có thể xem tiến độ của bài học do họ tạo", HttpStatus.FORBIDDEN),
    BEAT_MAP_ASSET_NOT_FOUND(2011, "Tài nguyên BEATMAP không tồn tại", HttpStatus.NOT_FOUND),
    EXERCISE_NOT_FOUND(2012, "Bài tập không tồn tại",  HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
