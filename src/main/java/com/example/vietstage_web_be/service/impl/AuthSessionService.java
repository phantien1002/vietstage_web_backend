package com.example.vietstage_web_be.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-session-expiration-ms:604800000}")
    private long refreshSessionExpirationMs;

    private static final String SESSION_KEY_PREFIX = "auth:session:";

    /**
     * Creates a new Redis session.
     * @param sessionId the UUID for the session
     * @param userId the user ID
     * @param plainRefreshToken the plain-text refresh token generated
     */
    public void createSession(String sessionId, Long userId, String plainRefreshToken) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String hashedToken = passwordEncoder.encode(plainRefreshToken);
        
        // Value format: userId:hashedToken
        String value = userId + ":" + hashedToken;
        
        redisTemplate.opsForValue().set(key, value, Duration.ofMillis(refreshSessionExpirationMs));
    }

    /**
     * Validates a refresh token against the stored session.
     * @param sessionId the UUID of the session
     * @param plainRefreshToken the provided plain-text refresh token
     * @return the userId if valid, null if invalid or expired
     */
    public Long validateSessionAndGetUserId(String sessionId, String plainRefreshToken) {
        String key = SESSION_KEY_PREFIX + sessionId;
        String value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return null;
        }

        String[] parts = value.split(":");
        if (parts.length != 2) {
            return null;
        }

        Long userId = Long.parseLong(parts[0]);
        String hashedToken = parts[1];

        if (passwordEncoder.matches(plainRefreshToken, hashedToken)) {
            return userId;
        }

        return null;
    }

    /**
     * Revokes (deletes) a session.
     * @param sessionId the UUID of the session
     */
    public void revokeSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }

    /**
     * Checks if a session is currently active.
     * @param sessionId the UUID of the session
     * @return true if active, false otherwise
     */
    public boolean isSessionActive(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
