package com.preethi.banking.repository;

import com.preethi.banking.entity.Notification;
import com.preethi.banking.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            NotificationStatus status
    );

    long countByUserIdAndStatus(
            Long userId,
            NotificationStatus status
    );
}