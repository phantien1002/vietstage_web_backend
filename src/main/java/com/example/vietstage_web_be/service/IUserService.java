package com.example.vietstage_web_be.service;

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
     * Lấy danh sách học viên (role = LEARNER) với tìm kiếm, sắp xếp và phân trang.
     *
     * @param keyword        từ khóa tìm theo email hoặc fullName
     * @param isActive       lọc theo trạng thái hoạt động
     * @param pageNumber     số trang, bắt đầu từ 1
     * @param pageSize       số phần tử mỗi trang (tối đa 100)
     * @param sortBy         field sắp xếp: id | email | fullName | createdAt
     * @param sortDescending true = giảm dần, false = tăng dần
     */
    PageResponse<UserResponse> getLearners(
            String keyword, Boolean isActive,
            int pageNumber, int pageSize, String sortBy, boolean sortDescending);

    /**
     * Lấy danh sách giảng viên (role = INSTRUCTOR) với tìm kiếm, sắp xếp và phân trang.
     *
     * @param keyword        từ khóa tìm theo email hoặc fullName
     * @param isActive       lọc theo trạng thái hoạt động
     * @param pageNumber     số trang, bắt đầu từ 1
     * @param pageSize       số phần tử mỗi trang (tối đa 100)
     * @param sortBy         field sắp xếp: id | email | fullName | createdAt
     * @param sortDescending true = giảm dần, false = tăng dần
     */
    PageResponse<UserResponse> getInstructors(
            String keyword, Boolean isActive,
            int pageNumber, int pageSize, String sortBy, boolean sortDescending);
}
