package com.revhire;

import com.revhire.controller.NotificationController;
import com.revhire.dto.NotificationResponse;
import com.revhire.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void getUnreadCount_ReturnsMap() {
        when(authentication.getName()).thenReturn("alice");
        when(notificationService.getUnreadCount("alice")).thenReturn(3L);

        Map<String, Long> response = notificationController.getUnreadCount(authentication);

        assertEquals(3L, response.get("unreadCount"));
    }

    @Test
    void markAsRead_DelegatesToService() {
        NotificationResponse expected = new NotificationResponse();
        expected.setId(9L);

        when(authentication.getName()).thenReturn("alice");
        when(notificationService.markAsRead("alice", 9L)).thenReturn(expected);

        NotificationResponse actual = notificationController.markAsRead(9L, authentication);

        assertEquals(9L, actual.getId());
        verify(notificationService).markAsRead("alice", 9L);
    }

    @Test
    void getJobSeekerNotifications_DelegatesToService() {
        when(authentication.getName()).thenReturn("alice");
        when(notificationService.getNotifications("alice")).thenReturn(List.of(new NotificationResponse()));

        List<NotificationResponse> list = notificationController.getJobSeekerNotifications(authentication);

        assertEquals(1, list.size());
    }
}
