package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.AdminUserResponse;
import com.example.vietstage_web_be.dto.request.AdminUserUpdateRequest;
import java.util.List;

public interface IAdminUserService {
    List<AdminUserResponse> getAllUsers();
    void updateUserStatus(Long id, String status);
    void updateUser(Long id, AdminUserUpdateRequest request);
}
