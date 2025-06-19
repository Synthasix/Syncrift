package com.hexplatoon.syncrift_backend.dto;

import com.hexplatoon.syncrift_backend.entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class NotificationDto {
    private Long id;
    private Long userId;
    private Long senderId;
    private String senderUsername;
    private String type;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationDto fromEntity(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setSenderId(notification.getSender().getId());
        dto.setSenderUsername(notification.getSender().getUsername());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}

