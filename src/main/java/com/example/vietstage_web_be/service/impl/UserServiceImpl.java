package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.AdminCreateRequest;
import com.example.vietstage_web_be.dto.request.InstructorCreateRequest;
import com.example.vietstage_web_be.dto.request.UpdateProfileRequest;
import com.example.vietstage_web_be.dto.response.AdminCreateResponse;
import com.example.vietstage_web_be.dto.response.InstructorCreateResponse;
import com.example.vietstage_web_be.dto.response.UserResponse;
import com.example.vietstage_web_be.entity.InstructorProfile;
import com.example.vietstage_web_be.entity.Role;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.RoleRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.service.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository UserRepository;
    private final RoleRepository RoleRepository;
    private final PasswordEncoder passwordEncoder;

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
        
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
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

        Long nextId = UserRepository.findTopByOrderByIdDesc().map(User::getId).orElse(0L) + 1;
        String generatedUserCode = String.format("GV-%04d", nextId);

        User user = new User();
        user.setUserCode(generatedUserCode);
        user.setRole(role);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); 
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

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userCode(user.getUserCode())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().getName())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        
    }

    @Override
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = UserRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setFcmToken(fcmToken);
        UserRepository.save(user);
    }
}
