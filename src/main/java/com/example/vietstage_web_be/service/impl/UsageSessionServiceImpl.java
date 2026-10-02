package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.entity.UsageSession;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.UsageSessionRepository;
import com.example.vietstage_web_be.service.IUsageSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsageSessionServiceImpl implements IUsageSessionService {

    private final UsageSessionRepository usageSessionRepository;

    @Override
    public UUID startSession(User user, String platform) {
        UsageSession session = UsageSession.builder()
                .user(user)
                .platform(platform != null ? platform : "WEB")
                .startedAt(LocalDateTime.now())
                .build();
        return usageSessionRepository.save(session).getUsageSessionId();
    }

    @Override
    public void endSession(User user, UUID sessionId) {
        UsageSession session = usageSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Session not found"));
        
        if (!session.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Not your session");
        }
        
        session.setEndedAt(LocalDateTime.now());
        usageSessionRepository.save(session);
    }

    @Override
    public void recordActivity(User user) {
        usageSessionRepository.findFirstByUser_IdOrderByStartedAtDesc(user.getId())
            .ifPresentOrElse(session -> {
                // If last activity was more than 30 minutes ago, create a new session
                if (session.getEndedAt() != null && session.getEndedAt().plusMinutes(30).isBefore(LocalDateTime.now())) {
                    startSession(user, "WEB");
                } else {
                    session.setEndedAt(LocalDateTime.now());
                    usageSessionRepository.save(session);
                }
            }, () -> {
                startSession(user, "WEB");
            });
    }
}
