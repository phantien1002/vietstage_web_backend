package com.example.vietstage_web_be.service.impl;

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
}
