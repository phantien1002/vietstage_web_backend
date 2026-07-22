package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.AdminCreateRequest;
import com.example.vietstage_web_be.dto.request.InstructorCreateRequest;
import com.example.vietstage_web_be.dto.request.UpdateProfileRequest;
import com.example.vietstage_web_be.dto.request.UpdateUserStatusRequest;
import com.example.vietstage_web_be.dto.response.AdminCreateResponse;
import com.example.vietstage_web_be.dto.response.InstructorCreateResponse;
import com.example.vietstage_web_be.dto.response.PageResponse;
import com.example.vietstage_web_be.dto.response.UserResponse;
import com.example.vietstage_web_be.entity.InstructorProfile;
import com.example.vietstage_web_be.entity.Role;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.RoleRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.service.IUserService;
import com.example.vietstage_web_be.specification.UserSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    private final UserRepository UserRepository;
    private final RoleRepository RoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getUserById(Long id) {
        User user = UserRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + id));
        return toUserResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getUsers(
            String keyword, String role, Boolean isActive,
            int pageNumber, int pageSize, String sortBy, boolean sortDescending) {

        Pageable pageable = buildPageable(pageNumber, pageSize, sortBy, sortDescending);
        Specification<User> spec = UserSpecification.filter(keyword, role, isActive);
        return toPageResponse(UserRepository.findAll(spec, pageable));
    }

    @Override
    public UserResponse getMyProfile(String email) {
        User user = UserRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với email: " + email));
        return toUserResponse(user);
    }

    @Override
    public UserResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = UserRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với email: " + email));
        user.setFullName(request.getFullName());
        User savedUser = UserRepository.save(user);
        return toUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = UserRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng với id: " + id));
        user.setActive(request.getActive());
        User savedUser = UserRepository.save(user);
        return toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public InstructorCreateResponse createInstructor(InstructorCreateRequest request) {
        if (UserRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXIST, "Email đã tồn tại");
        }

        Role role = RoleRepository.findByName("INSTRUCTOR")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "Vai trò không tồn tại"));

        User user = new User();
        user.setRole(role);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Mã hóa bảo mật
        user.setFullName(request.getFullName());
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        InstructorProfile profile = new InstructorProfile();
        profile.setUser(user);
        profile.setBiography(request.getBiography());
        profile.setYearsExperience(request.getYearsExperience() != null ? request.getYearsExperience() : 0);
        profile.setUpdatedAt(LocalDateTime.now());

        user.setInstructorProfile(profile);

        User savedUser = UserRepository.save(user);

        return InstructorCreateResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .roleName(role.getName())
                .isActive(savedUser.getActive())
                .createdAt(savedUser.getCreatedAt())
                .biography(savedUser.getInstructorProfile().getBiography())
                .yearsExperience(savedUser.getInstructorProfile().getYearsExperience())
                .build();
    }

    @Override
    @Transactional
    public AdminCreateResponse createAdmin(AdminCreateRequest request) {
        if (UserRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXIST, "Email đã tồn tại");
        }

        Role role = RoleRepository.findByName("ADMIN")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "Vai trò ADMIN không tồn tại"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = UserRepository.save(user);
        return AdminCreateResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .roleName(role.getName())
                .isActive(savedUser.getActive())
                .createdAt(savedUser.getCreatedAt())
                .build();
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

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private PageResponse<UserResponse> toPageResponse(Page<User> usersPage) {
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
