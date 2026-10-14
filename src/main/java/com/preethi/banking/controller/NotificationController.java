package com.preethi.banking.controller;

import com.preethi.banking.entity.Notification;
import com.preethi.banking.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get all notifications for the logged-in user.
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                notificationService.getUserNotifications(userId)
        );
    }

    /**
     * Get unread notifications for the logged-in user.
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId)
        );
    }

    /**
     * Get unread notification count.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                notificationService.getUnreadNotificationCount(userId)
        );
    }

    /**
     * Mark one of the logged-in user's notifications as read.
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        userId,
                        notificationId
                )
        );
    }

    /**
     * Extract the application user ID from the authenticated user.
     *
     * At the moment Authentication contains the username.
     * We resolve the user ID through the service layer.
     */
    private Long getUserId(Authentication authentication) {

        return notificationService.getUserIdByUsername(
                authentication.getName()
        );
    }
}