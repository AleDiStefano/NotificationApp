package com.alejandro.notificationapp.messaging;

import com.alejandro.notificationapp.model.NotificationChannel;
import java.util.UUID;

/**
 * Evento de dominio publicado dentro de la transacción de creación de una
 * notificación. Se consume después del commit (ver {@link NotificationEventPublisher})
 * para no publicar a RabbitMQ algo que después podría hacer rollback.
 */
public record NotificationCreatedEvent(UUID notificationId, NotificationChannel channel) {}
