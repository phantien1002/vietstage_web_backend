package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.UserResponse;
import com.example.vietstage_web_be.entity.Users;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.UsersRepository;
import com.example.vietstage_web_be.service.IUserService;
import com.example.vietstage_web_be.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final int MAX_PAGE_SIZE = 100;

    // Field hợp lệ để sắp xếp, tránh lỗi runtime
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "email", "fullName", "role", "active", "createdAt"
    );

    private final UsersRepository usersRepository;

    @Override
    public UserResponse getUserById(Long id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + id));
        return toUserResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getUsers(
            String keyword, String role, Boolean isActive,
            int pageNumber, int pageSize, String sortBy, boolean sortDescending) {

        Pageable pageable = buildPageable(pageNumber, pageSize, sortBy, sortDescending);
        Specification<Users> spec = UserSpecification.filter(keyword, role, isActive);
        return toPageResponse(usersRepository.findAll(spec, pageable));
    }

    @Override
    public PageResponse<UserResponse> getLearners(
            String keyword, Boolean isActive,
            int pageNumber, int pageSize, String sortBy, boolean sortDescending) {

        Pageable pageable = buildPageable(pageNumber, pageSize, sortBy, sortDescending);
        Specification<Users> spec = UserSpecification.filter(keyword, "LEARNER", isActive);
        return toPageResponse(usersRepository.findAll(spec, pageable));
    }

    @Override
    public PageResponse<UserResponse> getInstructors(
            String keyword, Boolean isActive,
            int pageNumber, int pageSize, String sortBy, boolean sortDescending) {

        Pageable pageable = buildPageable(pageNumber, pageSize, sortBy, sortDescending);
        Specification<Users> spec = UserSpecification.filter(keyword, "INSTRUCTOR", isActive);
        return toPageResponse(usersRepository.findAll(spec, pageable));
    }

    // ============================================================
    //  Private helpers
    // ============================================================

    private Pageable buildPageable(int pageNumber, int pageSize, String sortBy, boolean sortDescending) {
        // pageNumber bắt đầu từ 1 (frontend) → chuyển sang 0-indexed cho Spring
        int zeroBasedPage = Math.max(pageNumber - 1, 0);

        // Giới hạn pageSize trong khoảng [1, 100]
        int clampedSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        // Validate sort field
        String validSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "id";

        Sort sort = sortDescending
                ? Sort.by(validSortBy).descending()
                : Sort.by(validSortBy).ascending();

        return PageRequest.of(zeroBasedPage, clampedSize, sort);
    }

    private UserResponse toUserResponse(Users user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private PageResponse<UserResponse> toPageResponse(Page<Users> usersPage) {
        List<UserResponse> content = usersPage.getContent()
                .stream()
                .map(this::toUserResponse)
                .toList();

        return PageResponse.<UserResponse>builder()
                .content(content)
                .page(usersPage.getNumber() + 1)   // trả về 1-indexed cho frontend
                .size(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .last(usersPage.isLast())
                .build();
    }
}
