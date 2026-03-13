package com.revhire.service;

import com.revhire.dto.NotificationResponse;
import com.revhire.entity.Notification;
import com.revhire.entity.User;
import com.revhire.repository.NotificationRepository;
import com.revhire.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    public long getUnreadCount(String username) {
        User user = findUser(username);
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setJobId(notification.getJob() == null ? null : notification.getJob().getId());
        response.setMessage(notification.getMessage());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }

    private User findUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
