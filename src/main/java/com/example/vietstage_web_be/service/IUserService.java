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
     * Lấy thông tin một user theo id.
     */
    UserResponse getUserById(Long id);

    /**
     * Lấy danh sách tất cả người dùng với tìm kiếm, sắp xếp và phân trang.
     *
     * @param keyword        từ khóa tìm theo email hoặc fullName
     * @param role           lọc theo role: ADMIN | INSTRUCTOR | LEARNER
     * @param isActive       lọc theo trạng thái hoạt động
     * @param pageNumber     số trang, bắt đầu từ 1
     * @param pageSize       số phần tử mỗi trang (tối đa 100)
     * @param sortBy         field sắp xếp: id | email | fullName | role | createdAt
     * @param sortDescending true = giảm dần, false = tăng dần
     */
    PageResponse<UserResponse> getUsers(
            String keyword, String role, Boolean isActive,
            int pageNumber, int pageSize, String sortBy, boolean sortDescending);

    /**
     * Lấy thông tin profile của user hiện tại.
     */
    UserResponse getMyProfile(String email);

    /**
     * Cập nhật thông tin profile của user hiện tại.
     */
    UserResponse updateMyProfile(String email, UpdateProfileRequest request);

    /**
     * Cập nhật trạng thái kích hoạt/vô hiệu hóa của user theo ID.
     */
    UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request);


    /**
     * Admin tạo Instructor Account.
     */
    InstructorCreateResponse createInstructor(InstructorCreateRequest request);

    /**
     * Admin tạo Admin Account.
     */
    AdminCreateResponse createAdmin(AdminCreateRequest request);
}

