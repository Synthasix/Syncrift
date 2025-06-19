package com.hexplatoon.syncrift_backend.service;

import com.hexplatoon.syncrift_backend.dto.NotificationDto;
import com.hexplatoon.syncrift_backend.entity.Notification;
import com.hexplatoon.syncrift_backend.entity.User;
import com.hexplatoon.syncrift_backend.repository.NotificationRepository;
import com.hexplatoon.syncrift_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void createNotification(String recipientUsername, String senderUsername, String type, String message) {
        User user = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Notification notification = Notification.builder()
                .user(user)
                .sender(sender)
                .type(type)
                .message(message)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        NotificationDto dto = NotificationDto.fromEntity(savedNotification);
        // Send real-time notification via WebSocket
        messagingTemplate.convertAndSendToUser(
                recipientUsername,
            "/topic/notifications",
            dto
        );
    }

    public List<Notification> getUserNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void markNotificationAsRead(Long notificationId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().equals(user)) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllNotificationsAsRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalse(user);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}

