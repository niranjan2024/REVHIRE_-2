package com.revhire.service;

import com.revhire.dto.NotificationResponse;
import com.revhire.entity.Notification;
import com.revhire.entity.JobPosting;
import com.revhire.entity.NotificationType;
import com.revhire.entity.User;
import com.revhire.repository.NotificationRepository;
import com.revhire.repository.UserRepository;
import com.revhire.util.InputSanitizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public NotificationService(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> getNotifications(String username) {
        User user = findUser(username);
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> getNotificationsForUserId(String username, Long userId) {
        User user = findUser(username);
        if (!user.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can access only your notifications");
        }
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount(String username) {
        User user = findUser(username);
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    @Transactional
    public NotificationResponse markAsRead(String username, Long notificationId) {
        User user = findUser(username);
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    public void createNotification(User recipient, JobPosting job, String message) {
        createNotification(recipient, job, message, NotificationType.SYSTEM);
    }

    public void createNotification(User recipient, JobPosting job, String message, NotificationType type) {
        if (recipient == null || message == null || message.isBlank()) {
            return;
        }
        String sanitized = InputSanitizer.sanitize(message, "message");
        if (sanitized == null) {
            return;
        }
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200);
        }

        Optional<Notification> lastNotification = notificationRepository
                .findTopByRecipientAndMessageOrderByCreatedAtDesc(recipient, sanitized);
        if (lastNotification.isPresent()) {
            Duration diff = Duration.between(lastNotification.get().getCreatedAt(), java.time.LocalDateTime.now());
            if (diff.getSeconds() < 5) {
                return;
            }
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setJob(job);
        notification.setType(type == null ? NotificationType.SYSTEM : type);
        notification.setMessage(sanitized);
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setJobId(notification.getJob() == null ? null : notification.getJob().getId());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType() == null ? null : notification.getType().name());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }

    private User findUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
