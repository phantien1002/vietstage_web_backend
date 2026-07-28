package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.entity.InstructorProfile;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.RoleRepository;
import com.example.vietstage_web_be.entity.Role;
import com.example.vietstage_web_be.repository.InstructorProfileRepository;
import com.example.vietstage_web_be.repository.UserRepository;
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
