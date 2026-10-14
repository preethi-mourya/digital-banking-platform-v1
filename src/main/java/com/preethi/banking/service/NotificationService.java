
package com.preethi.banking.service;

import com.preethi.banking.entity.AuditAction;
import com.preethi.banking.entity.Notification;
import com.preethi.banking.entity.NotificationStatus;
import com.preethi.banking.entity.NotificationType;
import com.preethi.banking.entity.User;
import com.preethi.banking.exception.ResourceNotFoundException;
import com.preethi.banking.repository.NotificationRepository;
import com.preethi.banking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Notification createNotification(
            Long userId,
            String title,
            String message,
            NotificationType type) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId));

        Notification notification = new Notification(
                title,
                message,
                type,
                user
        );

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {

        validateUserExists(userId);

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {

        validateUserExists(userId);

        return notificationRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(
                        userId,
                        NotificationStatus.UNREAD
                );
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {

        validateUserExists(userId);

        return notificationRepository.countByUserIdAndStatus(
                userId,
                NotificationStatus.UNREAD
        );
    }

    @Transactional
    public Notification markAsRead(
            Long userId,
            Long notificationId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with id: "
                                        + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Notification not found for user: " + userId);
        }

        if (notification.getStatus() == NotificationStatus.UNREAD) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());

            Notification savedNotification =
                    notificationRepository.save(notification);

            auditLogService.record(
                    notification.getUser().getUsername(),
                    AuditAction.NOTIFICATION_READ,
                    "NOTIFICATION",
                    String.valueOf(savedNotification.getId()),
                    "Notification marked as read"
            );

            return savedNotification;
        }

        return notification;
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + userId);
        }
    }

    public Long getUserIdByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with username: " + username));

        return user.getId();
    }
}
