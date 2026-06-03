package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.UserResponse;

public interface IUserService {

    /**
     * Lấy danh sách tất cả người dùng với tìm kiếm, sắp xếp và phân trang.
     *
     * @param keyword   từ khóa tìm theo email hoặc fullName
     * @param role      lọc theo role (ADMIN, INSTRUCTOR, LEARNER)
     * @param isActive  lọc theo trạng thái hoạt động
     * @param page      số trang (bắt đầu từ 0)
     * @param size      số phần tử mỗi trang
     * @param sortBy    tên field cần sắp xếp (id, email, fullName, createdAt)
     * @param sortDir   chiều sắp xếp (asc hoặc desc)
     */
    PageResponse<UserResponse> getUsers(
            String keyword, String role, Boolean isActive,
            int page, int size, String sortBy, String sortDir);

    /**
     * Lấy danh sách học viên (role = LEARNER) với tìm kiếm, sắp xếp và phân trang.
     *
     * @param keyword   từ khóa tìm theo email hoặc fullName
     * @param isActive  lọc theo trạng thái hoạt động
     * @param page      số trang (bắt đầu từ 0)
     * @param size      số phần tử mỗi trang
     * @param sortBy    tên field cần sắp xếp (id, email, fullName, createdAt)
     * @param sortDir   chiều sắp xếp (asc hoặc desc)
     */
    PageResponse<UserResponse> getLearners(
            String keyword, Boolean isActive,
            int page, int size, String sortBy, String sortDir);
}
