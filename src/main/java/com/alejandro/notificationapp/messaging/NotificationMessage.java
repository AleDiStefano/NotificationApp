package com.alejandro.notificationapp.messaging;

import java.util.UUID;

/**
 * Payload publicado a RabbitMQ. Lleva sólo el id a propósito: el listener
 * relee la entidad fresca de la base en vez de confiar en un payload que
 * podría haber quedado desactualizado.
 */
public record NotificationMessage(UUID notificationId) {}
