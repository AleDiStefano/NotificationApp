package com.alejandro.notificationapp.dto;

import com.alejandro.notificationapp.model.NotificationStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para cambiar el estado de una notificación.
 */
public record UpdateNotificationStatusRequest(

    @NotNull
    NotificationStatus status
) {}
