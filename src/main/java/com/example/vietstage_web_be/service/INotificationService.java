package com.example.vietstage_web_be.service;

import com.example.vietstage_web_be.dto.response.NotificationListResponse;
import com.example.vietstage_web_be.dto.response.NotificationResponse;
import com.example.vietstage_web_be.entity.User;
import org.springframework.data.domain.Pageable;

public interface INotificationService {

    NotificationListResponse getNotifications(User user, Boolean isRead, Pageable pageable);

    NotificationResponse markAsRead(Long notificationId, User user);

    int markAllAsRead(User user);
    
    // Internal method to create notifications (e.g. after completing daily challenge)
    void createNotification(User user, String title, String message, String type);
}