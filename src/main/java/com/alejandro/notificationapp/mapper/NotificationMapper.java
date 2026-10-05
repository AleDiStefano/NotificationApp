package com.alejandro.notificationapp.mapper;

import com.alejandro.notificationapp.dto.NotificationResponse;
import com.alejandro.notificationapp.model.Notification;
import org.springframework.stereotype.Component;

/**
 * Conversión entre la entidad {@link Notification} y sus DTOs.
 */
@Component
public class NotificationMapper {

    private final UserMapper userMapper;

    public NotificationMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            userMapper.toResponse(notification.getRecipient()),
            notification.getSubject(),
            notification.getMessage(),
            notification.getChannel(),
            notification.getStatus(),
            notification.getCreatedAt(),
            notification.getUpdatedAt(),
            notification.getSentAt()
        );
    }
}
