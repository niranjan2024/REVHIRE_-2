package com.revhire;

import com.revhire.dto.NotificationResponse;
import com.revhire.entity.Notification;
import com.revhire.entity.NotificationType;
import com.revhire.entity.User;
import com.revhire.repository.NotificationRepository;
import com.revhire.repository.UserRepository;
import com.revhire.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import com.revhire.exception.ApiException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void getNotificationsForUserId_RejectsDifferentUser() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));

        ApiException ex = assertThrows(ApiException.class,
                () -> notificationService.getNotificationsForUserId("alice", 2L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void markAsRead_UpdatesUnreadNotification() {
        User user = new User();
        user.setId(1L);
        Notification notification = new Notification();
        notification.setId(10L);
        notification.setMessage("hello");
        notification.setType(NotificationType.SYSTEM);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));
        when(notificationRepository.findByIdAndRecipientId(10L, 1L)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markAsRead("alice", 10L);

        assertEquals(10L, response.getId());
        verify(notificationRepository).save(notification);
    }

    @Test
    void createNotification_IgnoresBlankMessage() {
        User user = new User();

        notificationService.createNotification(user, null, "   ");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void getNotifications_MapsToResponse() {
        User user = new User();
        Notification notification = new Notification();
        notification.setId(11L);
        notification.setMessage("msg");
        notification.setType(NotificationType.SYSTEM);
        notification.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));
        when(notificationRepository.findByRecipientOrderByCreatedAtDesc(user)).thenReturn(List.of(notification));

        List<NotificationResponse> list = notificationService.getNotifications("alice");

        assertEquals(1, list.size());
        assertEquals("msg", list.get(0).getMessage());
    }
}
