package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.CreateInstructorRequest;
import com.example.vietstage_web_be.dto.response.CreateInstructorResponse;
import com.example.vietstage_web_be.entity.InstructorProfiles;
import com.example.vietstage_web_be.entity.Users;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.RolesRepository;
import com.example.vietstage_web_be.entity.Roles;
import com.example.vietstage_web_be.repository.InstructorsRepository;
import com.example.vietstage_web_be.repository.UsersRepository;
import com.example.vietstage_web_be.service.IInstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstructorServiceImpl implements IInstructorService {
    private final InstructorsRepository instructorsRepository;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CreateInstructorResponse createInstructor(CreateInstructorRequest request) {
        if (usersRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXIST, "Email already exist");
        }

        Roles instructorRole = rolesRepository.findByName("INSTRUCTOR")
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Role INSTRUCTOR not found"));

        String generatedPassword =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 9);

        Users user = Users.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(generatedPassword))
                .fullName(request.getFullName())
                .role(instructorRole)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        usersRepository.save(user);

        InstructorProfiles profiles = InstructorProfiles.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .yearsExperience(request.getYearsExperience())
                .build();
        instructorsRepository.save(profiles);

        return CreateInstructorResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .fullName(user.getFullName())
                .specialization(profiles.getSpecialization())
                .yearsExperience(profiles.getYearsExperience())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
