package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.AdminUserResponse;
import java.util.List;

public interface IAdminUserService {
    List<AdminUserResponse> getAllUsers();
}
