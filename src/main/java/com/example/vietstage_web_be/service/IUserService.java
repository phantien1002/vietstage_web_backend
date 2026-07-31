package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.request.AdminCreateRequest;
import com.example.vietstage_web_be.dto.request.InstructorCreateRequest;
import com.example.vietstage_web_be.dto.request.UpdateProfileRequest;
import com.example.vietstage_web_be.dto.request.UpdateUserStatusRequest;
import com.example.vietstage_web_be.dto.response.AdminCreateResponse;
import com.example.vietstage_web_be.dto.response.InstructorCreateResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.UserResponse;


public interface IUserService {

    /**
     * Lấy thông tin profile của user hiện tại.
     */
    UserResponse getMyProfile(String email);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void updateFcmToken(Long userId, String fcmToken);

    /**
     * Cập nhật thông tin profile của user hiện tại.
     */
    UserResponse updateMyProfile(String email, UpdateProfileRequest request);


    /**
     * Admin tạo Instructor Account.
     */
    InstructorCreateResponse createInstructor(InstructorCreateRequest request);

    /**
     * Admin tạo Admin Account.
     */
    AdminCreateResponse createAdmin(AdminCreateRequest request);
}

