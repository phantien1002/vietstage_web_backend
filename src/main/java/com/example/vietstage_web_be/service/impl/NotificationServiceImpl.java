package com.example.vietstage_web_be.service.impl;

import com.example.vietstage_web_be.dto.response.NotificationListResponse;
import com.example.vietstage_web_be.dto.response.NotificationResponse;
import com.example.vietstage_web_be.entity.Notification;
import com.example.vietstage_web_be.entity.User;
import com.example.vietstage_web_be.exception.AppException;
import com.example.vietstage_web_be.exception.ErrorCode;
import com.example.vietstage_web_be.repository.NotificationRepository;
import com.example.vietstage_web_be.service.FCMService;
import com.example.vietstage_web_be.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final FCMService fcmService;

    @Override
    public NotificationListResponse getNotifications(User user, Boolean isRead, Pageable pageable) {
        Page<Notification> page;
        if (isRead != null) {
            page = notificationRepository.findByUserIdAndIsRead(user.getId(), isRead, pageable);
        } else {
            page = notificationRepository.findByUserId(user.getId(), pageable);
        }

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());

        List<NotificationResponse> data = page.getContent().stream().map(this::mapToResponse).toList();

        return NotificationListResponse.builder()
                .data(data)
                .unreadCount(unreadCount)
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        return mapToResponse(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(User user) {
        return notificationRepository.markAllAsRead(user.getId());
    }

    @Override
    @Transactional
    public void createNotification(User user, String title, String message, String type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        // Send push notification if token exists
        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
            fcmService.sendPushNotification(user.getFcmToken(), title, message);
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}