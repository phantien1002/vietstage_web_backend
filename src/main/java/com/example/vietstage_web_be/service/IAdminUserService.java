package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.AdminUserResponse;
import java.util.List;

import com.example.vietstage_web_be.dto.response.PageResponse;

public interface IAdminUserService {
    PageResponse<AdminUserResponse> getAllUsers(int page, int size, String search, String role, String sortBy, String sortDir);
    void updateUserStatus(Long id, String status);
}
