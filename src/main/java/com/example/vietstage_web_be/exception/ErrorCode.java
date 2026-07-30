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
    UNAUTHENTICATED(1011, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    LESSON_NOT_FOUND(2001, "Bài học không tồn tại", HttpStatus.NOT_FOUND),
    INSTRUMENT_NOT_FOUND(2002, "Nhạc cụ không tồn tại", HttpStatus.NOT_FOUND),
    TECHNIQUE_NOT_FOUND(2003, "Kỹ thuật không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_LESSON_ACCESS(2004, "Bạn không có quyền thao tác trên bài học này", HttpStatus.FORBIDDEN),
    INSTRUMENT_ALREADY_EXIST(2005, "Tên nhạc cụ đã tồn tại", HttpStatus.CONFLICT),
    TECHNIQUE_ALREADY_EXIST(2006, "Kỹ thuật đã tồn tại trong nhạc cụ này", HttpStatus.CONFLICT),
    LESSON_ALREADY_EXIST(2007, "Tiêu đề bài học đã tồn tại trong nhạc cụ này", HttpStatus.CONFLICT),
    INVALID_LESSON_STATUS(2008, "Trạng thái bài học không hợp lệ", HttpStatus.BAD_REQUEST),
    LESSON_STATUS_FORBIDDEN(2009, "Chỉ ADMIN mới có quyền duyệt/từ chối bài học", HttpStatus.FORBIDDEN),
    SKILL_LEVEL_NOT_FOUND(2010, "Trình độ không tồn tại", HttpStatus.NOT_FOUND),
    SKILL_LEVEL_CODE_ALREADY_EXIST(2011, "Mã trình độ đã tồn tại", HttpStatus.CONFLICT),
    SKILL_LEVEL_ORDER_ALREADY_EXIST(2012, "Thứ tự trình độ đã tồn tại", HttpStatus.CONFLICT),
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_SEND_FAILED(1012, "Không thể gửi email OTP", HttpStatus.INTERNAL_SERVER_ERROR),
    BEAT_MAP_ASSET_NOT_FOUND(2013, "Không tìm thấy asset beat map", HttpStatus.NOT_FOUND),
    EXERCISE_NOT_FOUND(2014, "Không tìm thấy bài tập", HttpStatus.NOT_FOUND),
    INSTRUCTOR_FORBIDDEN(2015, "Giảng viên không có quyền truy cập", HttpStatus.FORBIDDEN),
    QUIZ_NOT_FOUND(2016, "Không tìm thấy câu hỏi trắc nghiệm", HttpStatus.NOT_FOUND),
    MINIGAME_NOT_FOUND(2017, "Không tìm thấy minigame", HttpStatus.NOT_FOUND),
    ACHIEVEMENT_NOT_FOUND(2018, "Không tìm thấy thành tựu", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
