package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.entity.User;
import java.util.UUID;

public interface IUsageSessionService {
    UUID startSession(User user, String platform);
    void endSession(User user, UUID sessionId);
    void recordActivity(User user);
}
