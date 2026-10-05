package com.alejandro.notificationapp.dto;

import com.alejandro.notificationapp.model.NotificationChannel;
import com.alejandro.notificationapp.model.NotificationStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Vista pública de una notificación, con un resumen del destinatario.
 */
public record NotificationResponse(
    UUID id,
    UserResponse recipient,
    String subject,
    String message,
    NotificationChannel channel,
    NotificationStatus status,
    Instant createdAt,
    Instant updatedAt,
    Instant sentAt
) {}
