package com.revhire.controller;

import com.revhire.dto.NotificationResponse;
import com.revhire.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/jobseeker/notifications")
    public List<NotificationResponse> getJobSeekerNotifications(Authentication authentication) {
        return notificationService.getNotifications(authentication.getName());
    }

    @GetMapping("/employer/notifications")
    public List<NotificationResponse> getEmployerNotifications(Authentication authentication) {
        return notificationService.getNotifications(authentication.getName());
    }

    @GetMapping("/notifications/unread-count")
    public Map<String, Long> getUnreadCount(Authentication authentication) {
        return Map.of("unreadCount", notificationService.getUnreadCount(authentication.getName()));
    }
}
