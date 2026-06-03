package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.UserResponse;
import com.example.vietstage_web_be.entity.Users;
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

    // Danh sách field hợp lệ để sắp xếp, tránh SQL injection
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "email", "fullName", "role", "active", "createdAt"
    );

    private final UsersRepository usersRepository;

    @Override
    public PageResponse<UserResponse> getUsers(
            String keyword, String role, Boolean isActive,
            int page, int size, String sortBy, String sortDir) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<Users> spec = UserSpecification.filter(keyword, role, isActive);

        Page<Users> usersPage = usersRepository.findAll(spec, pageable);
        return toPageResponse(usersPage);
    }

    @Override
    public PageResponse<UserResponse> getLearners(
            String keyword, Boolean isActive,
            int page, int size, String sortBy, String sortDir) {

        // Cố định role = LEARNER
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<Users> spec = UserSpecification.filter(keyword, "LEARNER", isActive);

        Page<Users> usersPage = usersRepository.findAll(spec, pageable);
        return toPageResponse(usersPage);
    }

    // ============================================================
    //  Private helpers
    // ============================================================

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        // Validate sort field để tránh lỗi hoặc injection
        String validSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "id";
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(validSortBy).descending()
                : Sort.by(validSortBy).ascending();
        return PageRequest.of(page, size, sort);
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
                .page(usersPage.getNumber())
                .size(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .last(usersPage.isLast())
                .build();
    }
}
