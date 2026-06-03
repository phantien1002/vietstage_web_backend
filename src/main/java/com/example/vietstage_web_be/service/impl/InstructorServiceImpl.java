package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.request.CreateInstructorRequest;
import com.example.vietstage_web_be.dto.response.CreateInstructorResponse;
import com.example.vietstage_web_be.entity.InstructorProfiles;
import com.example.vietstage_web_be.entity.Users;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.InstructorsRepository;
import com.example.vietstage_web_be.repository.UsersRepository;
import com.example.vietstage_web_be.service.IInstructorService;
import org.springframework.transaction.annotation.Transactional;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CreateInstructorResponse createInstructorAccount(CreateInstructorRequest request) {
        if (usersRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXIST, "Email already exist");
        }

        String rawPassword = UUID.randomUUID().toString().substring(0, 8);

        Users user = Users.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role("INSTRUCTOR")
                .fullName(request.getFullName())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        usersRepository.save(user);

        InstructorProfiles instructor = InstructorProfiles.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .yearsExperience(request.getYearsExperience())
                .build();
        instructorsRepository.save(instructor);

        return CreateInstructorResponse.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .generatedPassword(rawPassword)
                .fullName(user.getFullName())
                .specialization(instructor.getSpecialization())
                .createdAt(user.getCreatedAt())
                .message("Instructor created successfully")
                .build();
    }
}
