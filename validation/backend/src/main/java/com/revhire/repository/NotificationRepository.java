package com.revhire.repository;

import com.revhire.entity.Notification;
import com.revhire.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    long countByRecipientAndIsReadFalse(User recipient);
    Optional<Notification> findTopByRecipientAndMessageOrderByCreatedAtDesc(User recipient, String message);
}
