package com.alejandro.notificationapp.service;

import com.alejandro.notificationapp.dto.CreateNotificationRequest;
import com.alejandro.notificationapp.dto.NotificationResponse;
import com.alejandro.notificationapp.model.NotificationChannel;
import com.alejandro.notificationapp.model.NotificationStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Casos de uso sobre notificaciones.
 */
public interface NotificationService {

    NotificationResponse create(CreateNotificationRequest request);

    NotificationResponse getById(UUID id);

    Page<NotificationResponse> list(UUID recipientId, NotificationStatus status, NotificationChannel channel, Pageable pageable);

    NotificationResponse updateStatus(UUID id, NotificationStatus status);

    void delete(UUID id);
}
