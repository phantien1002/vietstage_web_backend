package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.entity.AuditLog;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.repository.AuditLogRepository;
import com.example.vietstage_web_be.repository.UserRepository;
import com.example.vietstage_web_be.service.IAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements IAuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    public void logAction(Long userId, String actionType, String entityType, String entityId, String description) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            AuditLog log = AuditLog.builder()
                    .user(userOpt.get())
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);
        }
    }
}
