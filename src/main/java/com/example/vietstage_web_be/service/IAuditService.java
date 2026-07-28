package com.example.vietstage_web_be.service;

public interface IAuditService {
    void logAction(Long userId, String actionType, String entityType, String entityId, String description);
}
