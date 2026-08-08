package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.AdminUserResponse;
import java.util.List;

import com.example.vietstage_web_be.dto.response.PageResponse;

import java.util.List;

public interface IAdminUserService {
    PageResponse<AdminUserResponse> getAllUsers(int page, int size, String search, List<String> roles, String status, String sortBy, String sortDir);
    void updateUserStatus(Long id, String status, Long currentUserId);
    void updateUserRole(Long id, String newRole, Long currentUserId);
    void updateUser(Long id, com.example.vietstage_web_be.dto.request.UpdateProfileRequest request);
    void resetPassword(Long id, String newPassword);
}
