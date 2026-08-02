package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.ActivityDto;
import com.example.vietstage_web_be.dto.response.AdminUserResponse;
import com.example.vietstage_web_be.dto.response.UserStatsDto;
import com.example.vietstage_web_be.entity.AuditLog;
import com.example.vietstage_web_be.entity.Instrument;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.repository.AuditLogRepository;
import com.example.vietstage_web_be.repository.LessonRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.service.IAdminUserService;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements IAdminUserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final LessonRepository lessonRepository;

    @Override
    public List<AdminUserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        return users.stream().map(user -> {
            String roleName = user.getRole().getName();
            String displayRole;
            if ("ADMIN".equalsIgnoreCase(roleName)) displayRole = "Admin";
            else if ("INSTRUCTOR".equalsIgnoreCase(roleName)) displayRole = "Giảng viên";
            else displayRole = "Người học";
            
            // Get initials
            String initials = getInitials(user.getFullName());
            
            // Get stats
            UserStatsDto stats = null;
            String specialty = null;
            List<String> instrumentsList = null;

            if ("INSTRUCTOR".equals(roleName)) {
                // Calculate stats
                int courses = user.getCreatedLessons() != null ? user.getCreatedLessons().size() : 0;
                
                // Calculate students (mocking logic or counting from practice attempts of these Lesson)
                // Assuming simple mockup for students and rating for now or basic aggregation
                int students = 0;
                double rating = 5.0; // Default or mock rating
                
                if (user.getCreatedLessons() != null) {
                    // This is a naive calculation, ideally done via custom query
                    students = user.getCreatedLessons().stream()
                        .mapToInt(l -> l.getExercises() != null ? l.getExercises().size() : 0) 
                        .sum(); // placeholder logic
                }
                
                stats = UserStatsDto.builder()
                        .courses(courses)
                        .students(String.valueOf(students))
                        .rating(rating)
                        .build();

                // Get specialty and instruments
                if (user.getInstructorProfile() != null && user.getInstructorProfile().getInstruments() != null) {
                    instrumentsList = user.getInstructorProfile().getInstruments().stream()
                            .map(Instrument::getName)
                            .collect(Collectors.toList());
                    
                    if (!instrumentsList.isEmpty()) {
                        specialty = "Giảng viên " + String.join(", ", instrumentsList);
                    }
                }
            }

            java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String registeredAtStr = user.getCreatedAt() != null ? user.getCreatedAt().format(dateFormatter) : "";

            java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            List<AuditLog> recentLogs = auditLogRepository.findTop3ByUserIdOrderByCreatedAtDesc(user.getId());
            List<ActivityDto> activities = recentLogs.stream().map(log -> ActivityDto.builder()
                    .title(log.getDescription())
                    .time(log.getCreatedAt() != null ? log.getCreatedAt().format(timeFormatter) : "")
                    .build()).collect(Collectors.toList());

            return AdminUserResponse.builder()
                    .id(user.getId())
                    .name(user.getFullName())
                    .email(user.getEmail())
                    .role(displayRole)
                    .registeredAt(registeredAtStr)
                    .status(Boolean.TRUE.equals(user.getActive()) ? "active" : "locked")
                    .avatar(user.getAvatarUrl())
                    .initials(initials)
                    .specialty(specialty)
                    .stats(stats)
                    .instruments(instrumentsList)
                    .activities(activities)
                    .build();
        }).collect(Collectors.toList());
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        String first = parts[0].substring(0, 1).toUpperCase();
        String last = parts[parts.length - 1].substring(0, 1).toUpperCase();
        return first + last;
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Người dùng không tồn tại"));

        if ("locked".equalsIgnoreCase(status)) {
            user.setActive(false);
            AuditLog log = AuditLog.builder()
                    .user(user)
                    .actionType("UPDATE_STATUS")
                    .entityType("USER")
                    .entityId(user.getId().toString())
                    .description("Tài khoản bị khóa bởi Admin")
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);
        } else {
            user.setActive(true);
            AuditLog log = AuditLog.builder()
                    .user(user)
                    .actionType("UPDATE_STATUS")
                    .entityType("USER")
                    .entityId(user.getId().toString())
                    .description("Tài khoản được mở khóa bởi Admin")
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);
        }
        
        userRepository.save(user);
    }
}

