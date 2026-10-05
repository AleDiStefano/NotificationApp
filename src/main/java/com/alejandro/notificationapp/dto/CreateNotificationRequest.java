package com.alejandro.notificationapp.dto;

import com.alejandro.notificationapp.model.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Datos de entrada para crear una notificación.
 */
public record CreateNotificationRequest(

    @NotNull
    UUID recipientId,

    @NotBlank @Size(max = 150)
    String subject,

    @NotBlank @Size(max = 2000)
    String message,

    @NotNull
    NotificationChannel channel
) {}
